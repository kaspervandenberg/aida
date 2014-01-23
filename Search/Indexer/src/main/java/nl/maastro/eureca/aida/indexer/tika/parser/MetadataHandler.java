// © Maastro, 2013
package nl.maastro.eureca.aida.indexer.tika.parser;

import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Deque;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.Map;
import org.apache.tika.metadata.DublinCore;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.Property;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * {@link ZylabMetadataXml} uses this class to handle SAX events when parsing
 * a XmlFields-file.
 *
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 */
class MetadataHandler extends DefaultHandler {
	/**
	 * Tags and attributes recognised in XmlFields by {@link State}.
	 */
	private static enum XmlTags {
		TAG_DOC("document"),
		TAG_FIELD("field");

		private final String s;

		private XmlTags(final String s_) {
			s = s_;
		}

		/**
		 * Compare to {@code other} to {@code this} tag.
		 * 
		 * @param other	String to match
		 * 
		 * @return <ul><li>{@code true}, this tag matches {@code other}; or</li>
		 * 		<li>{@code false}, this and {@code other} differ</li></ul>
		 */
		public boolean equalTo(final String other) {
			return s.equalsIgnoreCase(other);
		}

	}

	/**
	 * Xml attributes recognised in XmlFields by {@link State}.
	 */
	private static enum XmlAttributes {
		ATTR_PATH("path"),
		ATTR_NAME("name"),
		ATTR_KEY("key"),
		ATTR_DOC_DATE("date"),
		ATTR_ID("id");
		
		private static final Map<XmlAttributes, Property> STORED_IN_METADATA;
		static {
			EnumMap<XmlAttributes, Property>	 result = 
					new EnumMap<>(XmlAttributes.class);
			result.put(ATTR_KEY, DublinCore.SUBJECT);
			result.put(ATTR_DOC_DATE, DublinCore.DATE);

			STORED_IN_METADATA = Collections.unmodifiableMap(result);
		}

		private final String s;

		private XmlAttributes(final String s_) {
			s = s_;
		}

		/**
		 * Retrieve the value this attribute has in {@code attr}.
		 * 
		 * @param attrs	{@link org.xml.sax.Attributes} that contains a matching 
		 * 		attribute.
		 * @return <ul><li>a {@link java.lang.String}, with the value of  the
		 * 		matching attribute ∈ {@code attrs}; or</li>
		 * 		<li>{@code null}, when {@code attrs} does not contain a 
		 * 		matching attribute.
		 */
		public String getValue(Attributes attrs) {
			return attrs.getValue("", s);
		}
		
		/**
		 * @return	the attributes that should be stored in 
		 * 		{@link #extractedMetadata} mapped to the 
		 * 		{@link org.apache.tika.metadata.Property} they should be 
		 * 		stored under.
		 */		
		public static Map<XmlAttributes, Property> getStoredInMetadata() {
			return STORED_IN_METADATA;
		}
	}
	
	/**
	 * {@link MetadataHandler} is implemented using a state pattern [GoF:395].
	 * 
	 * {@code State} uses the flyweight-pattern [GoF:??] to share State objects 
	 * between {@link MetadataHandler}s
	 */
	private static enum State {
		EXPECT_DOCUMENT {
			/**
			 * Set {@code contexts.}{@link MetadataHandler#about about},
			 * and add all atributes of {@link XmlAttributes#getStoredInMetadata intrest}
			 * to {@code context.}{@link MetadataHandler#extractedMetadata extractedMetadata}.
			 */
			@Override
			public State startElement(
					MetadataHandler context, String uri, String localName,
					String qName, Attributes attributes)
					throws SAXException {
				if(XmlTags.TAG_DOC.equalTo(localName)) {
					String refPath = XmlAttributes.ATTR_PATH.getValue(attributes);
					String refName = XmlAttributes.ATTR_NAME.getValue(attributes);
					context.about = new ZylabMetadataXml.FileRef(refPath, refName);

					for (XmlAttributes attr : XmlAttributes.getStoredInMetadata().keySet()) {
						String value = attr.getValue(attributes);
						if(value != null) {
							context.extractedMetadata.add(
									XmlAttributes.getStoredInMetadata().get(attr),
									value);
						}
					}
					return EXPECT_FIELD_OPEN_TAG;
				} else {
					return this;
				}
			}
		},
		
		EXPECT_FIELD_OPEN_TAG {
			/**
			 * Add a new {@link FieldParsingContents} to 
			 * {@code context.}{@link MetadataHandler#currentField currentField}.
			 */
			@Override
			public State startElement(MetadataHandler context, String uri, String localName, String qName, Attributes attributes) throws SAXException {
				if(XmlTags.TAG_FIELD.equalTo(localName)) {
					String id = XmlAttributes.ATTR_ID.getValue(attributes);
					context.currentField.push(new FieldParsingContents(id));
					return EXPECT_FIELD_CONTENTS;
				} else {
					return this;
				}
			}
			
		},
		
		EXPECT_FIELD_CONTENTS {

			/**
			 * Add the field to {@code context.}{@link MetadataHandler#extractedMetadata extractedMetadata}.
			 */
			@Override
			public State endElement(MetadataHandler context, String uri, String localName, String qName) throws SAXException {
				FieldParsingContents field = context.currentField.pop();
				Property prop = Property.get(field.fieldId);
				if(prop == null) {
					prop = Property.externalText(field.fieldId);
				}
				context.extractedMetadata.add(prop, field.getValue());
				
				return EXPECT_FIELD_OPEN_TAG;
			}

			/**
			 * Add some characters to {@code context.}{@link MetadataHandler#currentField currentField}.
			 */
			@Override
			public State characters(MetadataHandler context, char[] ch, int start, int length) throws SAXException {
				context.currentField.peek().append(ch, start, length);
				return this;
			}
		};
		
		/**
		 * Default implementation of {@link org.xml.sax.ContentHandler#startElement},
		 * does nothing and returns {@code this} as next {@link State}.
		 * 
		 * @param context	{@link MetadataHandler} that forwarded the SAXevent.
		 * 		{@code context} can be used, for example, to store extracted 
		 * 		{@link org.apache.tika.metadata.Metadata} (See flyweight-pattern}.
		 * @param uri		as {@code uri} in {@code ContentHandler.startElement}.
		 * @param localName	as {@code localName} in {@code ContentHandler.startElement}.
		 * @param qName		as {@code qName} in {@code ContentHandler.startElement}.
		 * @param attributes	as {@code attributes} in {@code ContentHandler.startElement}.
		 * 
		 * @return	the next {@link State} to which {@code context} should go.
		 * 
		 * @throws SAXException	as specified by {@code ContentHandler.startElement}
		 */
		public State startElement(final MetadataHandler context,
				String uri, String localName, String qName, Attributes attributes)
				throws SAXException {
			return this;
		}

		/**
		 * Default implementation of {@link org.xml.sax.ContentHandler#endElement},
		 * does nothing and returns {@code this} as next {@link State}.
		 * 
		 * @param context	{@link MetadataHandler} that forwarded the SAXevent.
		 * 		{@code context} can be used, for example, to store extracted 
		 * 		{@link org.apache.tika.metadata.Metadata} (See flyweight-pattern}.
		 * @param uri		as {@code uri} in {@code ContentHandler.endElement}.
		 * @param localName	as {@code localName} in {@code ContentHandler.endElement}.
		 * @param qName		as {@code qName} in {@code ContentHandler.endElement}.
		 * 
		 * @return	the next {@link State} to which {@code context} should go.
		 * 
		 * @throws SAXException as specified by {@code ContentHandler.endElement}
		 */
		public State endElement(final MetadataHandler context,
				String uri, String localName, String qName)
				throws SAXException {
			return this;
		}

		/**
		 * Default implementation of {@link org.xml.sax.ContentHandler#characters},
		 * does nothing and returns {@code this} as next {@link State}.
		 * 
		 * @param context	{@link MetadataHandler} that forwarded the SAXevent.
		 * 		{@code context} can be used, for example, to store extracted 
		 * 		{@link org.apache.tika.metadata.Metadata} (See flyweight-pattern}.
		 * @param ch		as {@code ch} in {@code ContentHandler.characters}.
		 * @param start		as {@code start} in {@code ContentHandler.characters}.
		 * @param length	as {@code length} in {@code ContentHandler.characters}.
		 * 
		 * @return	the next {@link State} to which {@code context} should go.
		 * 
		 * @throws SAXException as specified by {@code ContentHandler.characters}
		 */
		public State characters(final MetadataHandler context,
				char[] ch, int start, int length)
				throws SAXException {
			return this;
		}
	}

	private static class FieldParsingContents {
		public final String fieldId;
		private final StringBuilder valueBuilder = new StringBuilder();

		public FieldParsingContents(String fieldId_) {
			this.fieldId = fieldId_;
		}

		public void append(char ch[], int offset, int len) {
			valueBuilder.append(ch, offset, len);
		}

		public String getValue() {
			return valueBuilder.toString();
		}
	}

	/**
	 * Store the fields encountered while handling the SAX events.
	 */
	private final Metadata extractedMetadata;

	/**
	 * The file of which {@code this} is the metadata.
	 */
	private ZylabMetadataXml.FileRef about;

	private State state = State.EXPECT_DOCUMENT;

	private final Deque<FieldParsingContents> currentField = new LinkedList<>();

	public MetadataHandler(Metadata extractedMetadata_) {
		this.extractedMetadata = extractedMetadata_;
	}

	public ZylabMetadataXml.FileRef getAboutDocument() {
		return about;
	}
	
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		state = state.startElement(this, uri, localName, qName, attributes);
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		state = state.endElement(this, uri, localName, qName);
	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		state = state.characters(this, ch, start, length);
	}
	
}