//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v1.0.5-b16-fcs 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2006.02.28 at 01:11:10 PM CET 
//


package config.aid.org.impl;

public class DocTypeImpl implements config.aid.org.DocType, com.sun.xml.bind.JAXBObject, config.aid.org.impl.runtime.UnmarshallableObject, config.aid.org.impl.runtime.XMLSerializable, config.aid.org.impl.runtime.ValidatableObject
{

    protected java.lang.String _FileType;
    protected com.sun.xml.bind.util.ListImpl _Field;
    public final static java.lang.Class version = (config.aid.org.impl.JAXBVersion.class);
    private static com.sun.msv.grammar.Grammar schemaFragment;

    private final static java.lang.Class PRIMARY_INTERFACE_CLASS() {
        return (config.aid.org.DocType.class);
    }

    public java.lang.String getFileType() {
        return _FileType;
    }

    public void setFileType(java.lang.String value) {
        _FileType = value;
    }

    protected com.sun.xml.bind.util.ListImpl _getField() {
        if (_Field == null) {
            _Field = new com.sun.xml.bind.util.ListImpl(new java.util.ArrayList());
        }
        return _Field;
    }

    public java.util.List getField() {
        return _getField();
    }

    public config.aid.org.impl.runtime.UnmarshallingEventHandler createUnmarshaller(config.aid.org.impl.runtime.UnmarshallingContext context) {
        return new config.aid.org.impl.DocTypeImpl.Unmarshaller(context);
    }

    public void serializeBody(config.aid.org.impl.runtime.XMLSerializer context)
        throws org.xml.sax.SAXException
    {
        int idx2 = 0;
        final int len2 = ((_Field == null)? 0 :_Field.size());
        while (idx2 != len2) {
            context.startElement("http://aid.org/", "Field");
            int idx_0 = idx2;
            context.childAsURIs(((com.sun.xml.bind.JAXBObject) _Field.get(idx_0 ++)), "Field");
            context.endNamespaceDecls();
            int idx_1 = idx2;
            context.childAsAttributes(((com.sun.xml.bind.JAXBObject) _Field.get(idx_1 ++)), "Field");
            context.endAttributes();
            context.childAsBody(((com.sun.xml.bind.JAXBObject) _Field.get(idx2 ++)), "Field");
            context.endElement();
        }
    }

    public void serializeAttributes(config.aid.org.impl.runtime.XMLSerializer context)
        throws org.xml.sax.SAXException
    {
        int idx2 = 0;
        final int len2 = ((_Field == null)? 0 :_Field.size());
        if (_FileType!= null) {
            context.startAttribute("", "FileType");
            try {
                context.text(((java.lang.String) _FileType), "FileType");
            } catch (java.lang.Exception e) {
                config.aid.org.impl.runtime.Util.handlePrintConversionException(this, e, context);
            }
            context.endAttribute();
        }
        while (idx2 != len2) {
            idx2 += 1;
        }
    }

    public void serializeURIs(config.aid.org.impl.runtime.XMLSerializer context)
        throws org.xml.sax.SAXException
    {
        int idx2 = 0;
        final int len2 = ((_Field == null)? 0 :_Field.size());
        while (idx2 != len2) {
            idx2 += 1;
        }
    }

    public java.lang.Class getPrimaryInterface() {
        return (config.aid.org.DocType.class);
    }

    public com.sun.msv.verifier.DocumentDeclaration createRawValidator() {
        if (schemaFragment == null) {
            schemaFragment = com.sun.xml.bind.validator.SchemaDeserializer.deserialize((
 "\u00ac\u00ed\u0000\u0005sr\u0000\u001fcom.sun.msv.grammar.SequenceExp\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0000xr\u0000\u001dcom.su"
+"n.msv.grammar.BinaryExp\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0002L\u0000\u0004exp1t\u0000 Lcom/sun/msv/gra"
+"mmar/Expression;L\u0000\u0004exp2q\u0000~\u0000\u0002xr\u0000\u001ecom.sun.msv.grammar.Expressi"
+"on\u00f8\u0018\u0082\u00e8N5~O\u0002\u0000\u0002L\u0000\u0013epsilonReducibilityt\u0000\u0013Ljava/lang/Boolean;L\u0000\u000b"
+"expandedExpq\u0000~\u0000\u0002xpppsr\u0000\u001dcom.sun.msv.grammar.ChoiceExp\u0000\u0000\u0000\u0000\u0000\u0000\u0000"
+"\u0001\u0002\u0000\u0000xq\u0000~\u0000\u0001ppsr\u0000 com.sun.msv.grammar.OneOrMoreExp\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0000x"
+"r\u0000\u001ccom.sun.msv.grammar.UnaryExp\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0001L\u0000\u0003expq\u0000~\u0000\u0002xq\u0000~\u0000\u0003s"
+"r\u0000\u0011java.lang.Boolean\u00cd r\u0080\u00d5\u009c\u00fa\u00ee\u0002\u0000\u0001Z\u0000\u0005valuexp\u0000psr\u0000\'com.sun.msv.g"
+"rammar.trex.ElementPattern\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0001L\u0000\tnameClasst\u0000\u001fLcom/sun"
+"/msv/grammar/NameClass;xr\u0000\u001ecom.sun.msv.grammar.ElementExp\u0000\u0000\u0000"
+"\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0002Z\u0000\u001aignoreUndeclaredAttributesL\u0000\fcontentModelq\u0000~\u0000\u0002xq\u0000"
+"~\u0000\u0003q\u0000~\u0000\fp\u0000sq\u0000~\u0000\u0000ppsq\u0000~\u0000\rpp\u0000sq\u0000~\u0000\u0006ppsq\u0000~\u0000\bq\u0000~\u0000\fpsr\u0000 com.sun.m"
+"sv.grammar.AttributeExp\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0002L\u0000\u0003expq\u0000~\u0000\u0002L\u0000\tnameClassq\u0000~"
+"\u0000\u000exq\u0000~\u0000\u0003q\u0000~\u0000\fpsr\u00002com.sun.msv.grammar.Expression$AnyStringEx"
+"pression\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0000xq\u0000~\u0000\u0003sq\u0000~\u0000\u000b\u0001psr\u0000 com.sun.msv.grammar.Any"
+"NameClass\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0000xr\u0000\u001dcom.sun.msv.grammar.NameClass\u0000\u0000\u0000\u0000\u0000\u0000\u0000"
+"\u0001\u0002\u0000\u0000xpsr\u00000com.sun.msv.grammar.Expression$EpsilonExpression\u0000\u0000"
+"\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0000xq\u0000~\u0000\u0003q\u0000~\u0000\u0019psr\u0000#com.sun.msv.grammar.SimpleNameClass"
+"\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0002L\u0000\tlocalNamet\u0000\u0012Ljava/lang/String;L\u0000\fnamespaceURIq"
+"\u0000~\u0000 xq\u0000~\u0000\u001bt\u0000\u0018config.aid.org.FieldTypet\u0000+http://java.sun.com/"
+"jaxb/xjc/dummy-elementssq\u0000~\u0000\u0006ppsq\u0000~\u0000\u0015q\u0000~\u0000\fpsr\u0000\u001bcom.sun.msv.g"
+"rammar.DataExp\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0003L\u0000\u0002dtt\u0000\u001fLorg/relaxng/datatype/Datat"
+"ype;L\u0000\u0006exceptq\u0000~\u0000\u0002L\u0000\u0004namet\u0000\u001dLcom/sun/msv/util/StringPair;xq\u0000"
+"~\u0000\u0003ppsr\u0000\"com.sun.msv.datatype.xsd.QnameType\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0000xr\u0000*co"
+"m.sun.msv.datatype.xsd.BuiltinAtomicType\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0000xr\u0000%com.s"
+"un.msv.datatype.xsd.ConcreteType\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0000xr\u0000\'com.sun.msv.d"
+"atatype.xsd.XSDatatypeImpl\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0003L\u0000\fnamespaceUriq\u0000~\u0000 L\u0000\b"
+"typeNameq\u0000~\u0000 L\u0000\nwhiteSpacet\u0000.Lcom/sun/msv/datatype/xsd/White"
+"SpaceProcessor;xpt\u0000 http://www.w3.org/2001/XMLSchemat\u0000\u0005QName"
+"sr\u00005com.sun.msv.datatype.xsd.WhiteSpaceProcessor$Collapse\u0000\u0000\u0000"
+"\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0000xr\u0000,com.sun.msv.datatype.xsd.WhiteSpaceProcessor\u0000\u0000\u0000\u0000"
+"\u0000\u0000\u0000\u0001\u0002\u0000\u0000xpsr\u00000com.sun.msv.grammar.Expression$NullSetExpressio"
+"n\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0000xq\u0000~\u0000\u0003ppsr\u0000\u001bcom.sun.msv.util.StringPair\u00d0t\u001ejB\u008f\u008d\u00a0\u0002"
+"\u0000\u0002L\u0000\tlocalNameq\u0000~\u0000 L\u0000\fnamespaceURIq\u0000~\u0000 xpq\u0000~\u00001q\u0000~\u00000sq\u0000~\u0000\u001ft\u0000\u0004"
+"typet\u0000)http://www.w3.org/2001/XMLSchema-instanceq\u0000~\u0000\u001esq\u0000~\u0000\u001ft"
+"\u0000\u0005Fieldt\u0000\u000fhttp://aid.org/q\u0000~\u0000\u001esq\u0000~\u0000\u0006ppsq\u0000~\u0000\u0015q\u0000~\u0000\fpsq\u0000~\u0000&ppsr"
+"\u0000#com.sun.msv.datatype.xsd.StringType\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0001Z\u0000\risAlwaysV"
+"alidxq\u0000~\u0000+q\u0000~\u00000t\u0000\u0006stringsr\u00005com.sun.msv.datatype.xsd.WhiteSp"
+"aceProcessor$Preserve\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0000xq\u0000~\u00003\u0001q\u0000~\u00006sq\u0000~\u00007q\u0000~\u0000Dq\u0000~\u00000"
+"sq\u0000~\u0000\u001ft\u0000\bFileTypet\u0000\u0000q\u0000~\u0000\u001esr\u0000\"com.sun.msv.grammar.ExpressionP"
+"ool\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0001L\u0000\bexpTablet\u0000/Lcom/sun/msv/grammar/ExpressionP"
+"ool$ClosedHash;xpsr\u0000-com.sun.msv.grammar.ExpressionPool$Clos"
+"edHash\u00d7j\u00d0N\u00ef\u00e8\u00ed\u001c\u0003\u0000\u0003I\u0000\u0005countB\u0000\rstreamVersionL\u0000\u0006parentt\u0000$Lcom/su"
+"n/msv/grammar/ExpressionPool;xp\u0000\u0000\u0000\b\u0001pq\u0000~\u0000\nq\u0000~\u0000\u0005q\u0000~\u0000\u0011q\u0000~\u0000$q\u0000~"
+"\u0000\u0013q\u0000~\u0000\u0007q\u0000~\u0000\u0014q\u0000~\u0000?x"));
        }
        return new com.sun.msv.verifier.regexp.REDocumentDeclaration(schemaFragment);
    }

    public class Unmarshaller
        extends config.aid.org.impl.runtime.AbstractUnmarshallingEventHandlerImpl
    {


        public Unmarshaller(config.aid.org.impl.runtime.UnmarshallingContext context) {
            super(context, "-------");
        }

        protected Unmarshaller(config.aid.org.impl.runtime.UnmarshallingContext context, int startState) {
            this(context);
            state = startState;
        }

        public java.lang.Object owner() {
            return config.aid.org.impl.DocTypeImpl.this;
        }

        public void enterElement(java.lang.String ___uri, java.lang.String ___local, java.lang.String ___qname, org.xml.sax.Attributes __atts)
            throws org.xml.sax.SAXException
        {
            int attIdx;
            outer:
            while (true) {
                switch (state) {
                    case  3 :
                        if (("Field" == ___local)&&("http://aid.org/" == ___uri)) {
                            context.pushAttributes(__atts, false);
                            state = 4;
                            return ;
                        }
                        state = 6;
                        continue outer;
                    case  0 :
                        attIdx = context.getAttribute("", "FileType");
                        if (attIdx >= 0) {
                            final java.lang.String v = context.eatAttribute(attIdx);
                            state = 3;
                            eatText1(v);
                            continue outer;
                        }
                        state = 3;
                        continue outer;
                    case  6 :
                        if (("Field" == ___local)&&("http://aid.org/" == ___uri)) {
                            context.pushAttributes(__atts, false);
                            state = 4;
                            return ;
                        }
                        revertToParentFromEnterElement(___uri, ___local, ___qname, __atts);
                        return ;
                    case  4 :
                        attIdx = context.getAttribute("", "Type");
                        if (attIdx >= 0) {
                            context.consumeAttribute(attIdx);
                            context.getCurrentHandler().enterElement(___uri, ___local, ___qname, __atts);
                            return ;
                        }
                        if (("Title" == ___local)&&("http://aid.org/" == ___uri)) {
                            _getField().add(((config.aid.org.impl.FieldTypeImpl) spawnChildFromEnterElement((config.aid.org.impl.FieldTypeImpl.class), 5, ___uri, ___local, ___qname, __atts)));
                            return ;
                        }
                        if (("Author" == ___local)&&("http://aid.org/" == ___uri)) {
                            _getField().add(((config.aid.org.impl.FieldTypeImpl) spawnChildFromEnterElement((config.aid.org.impl.FieldTypeImpl.class), 5, ___uri, ___local, ___qname, __atts)));
                            return ;
                        }
                        if (("FirstAuthor" == ___local)&&("http://aid.org/" == ___uri)) {
                            _getField().add(((config.aid.org.impl.FieldTypeImpl) spawnChildFromEnterElement((config.aid.org.impl.FieldTypeImpl.class), 5, ___uri, ___local, ___qname, __atts)));
                            return ;
                        }
                        if (("Abstract" == ___local)&&("http://aid.org/" == ___uri)) {
                            _getField().add(((config.aid.org.impl.FieldTypeImpl) spawnChildFromEnterElement((config.aid.org.impl.FieldTypeImpl.class), 5, ___uri, ___local, ___qname, __atts)));
                            return ;
                        }
                        if (("Path" == ___local)&&("http://aid.org/" == ___uri)) {
                            _getField().add(((config.aid.org.impl.FieldTypeImpl) spawnChildFromEnterElement((config.aid.org.impl.FieldTypeImpl.class), 5, ___uri, ___local, ___qname, __atts)));
                            return ;
                        }
                        if (("Date" == ___local)&&("http://aid.org/" == ___uri)) {
                            _getField().add(((config.aid.org.impl.FieldTypeImpl) spawnChildFromEnterElement((config.aid.org.impl.FieldTypeImpl.class), 5, ___uri, ___local, ___qname, __atts)));
                            return ;
                        }
                        if (("URL" == ___local)&&("http://aid.org/" == ___uri)) {
                            _getField().add(((config.aid.org.impl.FieldTypeImpl) spawnChildFromEnterElement((config.aid.org.impl.FieldTypeImpl.class), 5, ___uri, ___local, ___qname, __atts)));
                            return ;
                        }
                        if (("ID" == ___local)&&("http://aid.org/" == ___uri)) {
                            _getField().add(((config.aid.org.impl.FieldTypeImpl) spawnChildFromEnterElement((config.aid.org.impl.FieldTypeImpl.class), 5, ___uri, ___local, ___qname, __atts)));
                            return ;
                        }
                        if (("PublishedIn" == ___local)&&("http://aid.org/" == ___uri)) {
                            _getField().add(((config.aid.org.impl.FieldTypeImpl) spawnChildFromEnterElement((config.aid.org.impl.FieldTypeImpl.class), 5, ___uri, ___local, ___qname, __atts)));
                            return ;
                        }
                        if (("Keywords" == ___local)&&("http://aid.org/" == ___uri)) {
                            _getField().add(((config.aid.org.impl.FieldTypeImpl) spawnChildFromEnterElement((config.aid.org.impl.FieldTypeImpl.class), 5, ___uri, ___local, ___qname, __atts)));
                            return ;
                        }
                        if (("MetaData" == ___local)&&("http://aid.org/" == ___uri)) {
                            _getField().add(((config.aid.org.impl.FieldTypeImpl) spawnChildFromEnterElement((config.aid.org.impl.FieldTypeImpl.class), 5, ___uri, ___local, ___qname, __atts)));
                            return ;
                        }
                        if (("FieldAnalyzer" == ___local)&&("http://aid.org/" == ___uri)) {
                            _getField().add(((config.aid.org.impl.FieldTypeImpl) spawnChildFromEnterElement((config.aid.org.impl.FieldTypeImpl.class), 5, ___uri, ___local, ___qname, __atts)));
                            return ;
                        }
                        if (("IndexStore" == ___local)&&("http://aid.org/" == ___uri)) {
                            _getField().add(((config.aid.org.impl.FieldTypeImpl) spawnChildFromEnterElement((config.aid.org.impl.FieldTypeImpl.class), 5, ___uri, ___local, ___qname, __atts)));
                            return ;
                        }
                        if (("TermVector" == ___local)&&("http://aid.org/" == ___uri)) {
                            _getField().add(((config.aid.org.impl.FieldTypeImpl) spawnChildFromEnterElement((config.aid.org.impl.FieldTypeImpl.class), 5, ___uri, ___local, ___qname, __atts)));
                            return ;
                        }
                        _getField().add(((config.aid.org.impl.FieldTypeImpl) spawnChildFromEnterElement((config.aid.org.impl.FieldTypeImpl.class), 5, ___uri, ___local, ___qname, __atts)));
                        return ;
                }
                super.enterElement(___uri, ___local, ___qname, __atts);
                break;
            }
        }

        private void eatText1(final java.lang.String value)
            throws org.xml.sax.SAXException
        {
            try {
                _FileType = value;
            } catch (java.lang.Exception e) {
                handleParseConversionException(e);
            }
        }

        public void leaveElement(java.lang.String ___uri, java.lang.String ___local, java.lang.String ___qname)
            throws org.xml.sax.SAXException
        {
            int attIdx;
            outer:
            while (true) {
                switch (state) {
                    case  5 :
                        if (("Field" == ___local)&&("http://aid.org/" == ___uri)) {
                            context.popAttributes();
                            state = 6;
                            return ;
                        }
                        break;
                    case  3 :
                        state = 6;
                        continue outer;
                    case  0 :
                        attIdx = context.getAttribute("", "FileType");
                        if (attIdx >= 0) {
                            final java.lang.String v = context.eatAttribute(attIdx);
                            state = 3;
                            eatText1(v);
                            continue outer;
                        }
                        state = 3;
                        continue outer;
                    case  6 :
                        revertToParentFromLeaveElement(___uri, ___local, ___qname);
                        return ;
                    case  4 :
                        attIdx = context.getAttribute("", "Type");
                        if (attIdx >= 0) {
                            context.consumeAttribute(attIdx);
                            context.getCurrentHandler().leaveElement(___uri, ___local, ___qname);
                            return ;
                        }
                        _getField().add(((config.aid.org.impl.FieldTypeImpl) spawnChildFromLeaveElement((config.aid.org.impl.FieldTypeImpl.class), 5, ___uri, ___local, ___qname)));
                        return ;
                }
                super.leaveElement(___uri, ___local, ___qname);
                break;
            }
        }

        public void enterAttribute(java.lang.String ___uri, java.lang.String ___local, java.lang.String ___qname)
            throws org.xml.sax.SAXException
        {
            int attIdx;
            outer:
            while (true) {
                switch (state) {
                    case  3 :
                        state = 6;
                        continue outer;
                    case  0 :
                        if (("FileType" == ___local)&&("" == ___uri)) {
                            state = 1;
                            return ;
                        }
                        state = 3;
                        continue outer;
                    case  6 :
                        revertToParentFromEnterAttribute(___uri, ___local, ___qname);
                        return ;
                    case  4 :
                        if (("Type" == ___local)&&("" == ___uri)) {
                            _getField().add(((config.aid.org.impl.FieldTypeImpl) spawnChildFromEnterAttribute((config.aid.org.impl.FieldTypeImpl.class), 5, ___uri, ___local, ___qname)));
                            return ;
                        }
                        _getField().add(((config.aid.org.impl.FieldTypeImpl) spawnChildFromEnterAttribute((config.aid.org.impl.FieldTypeImpl.class), 5, ___uri, ___local, ___qname)));
                        return ;
                }
                super.enterAttribute(___uri, ___local, ___qname);
                break;
            }
        }

        public void leaveAttribute(java.lang.String ___uri, java.lang.String ___local, java.lang.String ___qname)
            throws org.xml.sax.SAXException
        {
            int attIdx;
            outer:
            while (true) {
                switch (state) {
                    case  3 :
                        state = 6;
                        continue outer;
                    case  0 :
                        attIdx = context.getAttribute("", "FileType");
                        if (attIdx >= 0) {
                            final java.lang.String v = context.eatAttribute(attIdx);
                            state = 3;
                            eatText1(v);
                            continue outer;
                        }
                        state = 3;
                        continue outer;
                    case  6 :
                        revertToParentFromLeaveAttribute(___uri, ___local, ___qname);
                        return ;
                    case  2 :
                        if (("FileType" == ___local)&&("" == ___uri)) {
                            state = 3;
                            return ;
                        }
                        break;
                    case  4 :
                        attIdx = context.getAttribute("", "Type");
                        if (attIdx >= 0) {
                            context.consumeAttribute(attIdx);
                            context.getCurrentHandler().leaveAttribute(___uri, ___local, ___qname);
                            return ;
                        }
                        _getField().add(((config.aid.org.impl.FieldTypeImpl) spawnChildFromLeaveAttribute((config.aid.org.impl.FieldTypeImpl.class), 5, ___uri, ___local, ___qname)));
                        return ;
                }
                super.leaveAttribute(___uri, ___local, ___qname);
                break;
            }
        }

        public void handleText(final java.lang.String value)
            throws org.xml.sax.SAXException
        {
            int attIdx;
            outer:
            while (true) {
                try {
                    switch (state) {
                        case  3 :
                            state = 6;
                            continue outer;
                        case  0 :
                            attIdx = context.getAttribute("", "FileType");
                            if (attIdx >= 0) {
                                final java.lang.String v = context.eatAttribute(attIdx);
                                state = 3;
                                eatText1(v);
                                continue outer;
                            }
                            state = 3;
                            continue outer;
                        case  6 :
                            revertToParentFromText(value);
                            return ;
                        case  4 :
                            attIdx = context.getAttribute("", "Type");
                            if (attIdx >= 0) {
                                context.consumeAttribute(attIdx);
                                context.getCurrentHandler().text(value);
                                return ;
                            }
                            _getField().add(((config.aid.org.impl.FieldTypeImpl) spawnChildFromText((config.aid.org.impl.FieldTypeImpl.class), 5, value)));
                            return ;
                        case  1 :
                            state = 2;
                            eatText1(value);
                            return ;
                    }
                } catch (java.lang.RuntimeException e) {
                    handleUnexpectedTextException(value, e);
                }
                break;
            }
        }

    }

}
