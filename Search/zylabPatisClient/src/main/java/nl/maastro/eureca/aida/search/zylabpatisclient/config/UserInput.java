// © Maastro Clinic, 2013
package nl.maastro.eureca.aida.search.zylabpatisclient.config;

import checkers.nullness.quals.EnsuresNonNull;
import checkers.nullness.quals.NonNull;
import checkers.nullness.quals.Nullable;
import java.awt.Component;
import java.io.IOException;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;

/**
 * Allows asking the user a question
 *
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 */
public class UserInput {
	private interface RegularInput {
		public String getValue() throws IOException;
	}

	private interface PasswordInput {
		public char[] getPassword() throws IOException;
	}


	private enum Media {
		GUI {
			@Override
			public RegularInput createRegularInput(UserInput context) {
				return context.new GuiRegualar();
			}

			@Override
			public PasswordInput createPasswordInput(UserInput context) {
				return context.new GuiPassword();
			}
		},
		
		CONSOLE {
			@Override
			public RegularInput createRegularInput(UserInput context) {
				return context.new ConsoleRegular();
			}

			@Override
			public PasswordInput createPasswordInput(UserInput context) {
				return context.new ConsolePassword();
			}
		};

		public abstract RegularInput createRegularInput(UserInput context);
		public abstract PasswordInput createPasswordInput(UserInput context);

		public static Media selectMedium() {
			if (!java.awt.GraphicsEnvironment.isHeadless()) {
				return GUI;
			} else if (System.console() != null) {
				return CONSOLE;
			}
			throw new IllegalStateException("No suitable medium found to ask user a question.");
		}
	}	
	

	private abstract class Gui {
		@SuppressWarnings("nullness")	// showInputDialog allows null parents
		@NonNull
		protected final Component parent = null;
	
		protected abstract Object getInputContentsLine();

		protected Object[] createDialogContents() {
			return new Object[] {
				message,
				getInputContentsLine()
			};
		}
	}


	private class GuiRegualar extends Gui implements RegularInput {
		@Override
		public String getValue() {
			return JOptionPane.showInputDialog(
					parent,	createDialogContents(), title,
					JOptionPane.QUESTION_MESSAGE);
		}

		@Override
		protected Object getInputContentsLine() {
			return prompt;
		}
	}


	private class GuiPassword extends Gui implements PasswordInput {
		final JLabel lblPrompt;
		final JPasswordField password;
		final JPanel panel;

		public GuiPassword() {
			lblPrompt = new javax.swing.JLabel(prompt);
			password = new javax.swing.JPasswordField();
			password.setPreferredSize(new java.awt.Dimension(100,20));
			panel = new javax.swing.JPanel();
			panel.add(lblPrompt);
			panel.add(password);
		}
		
		@Override
		public char[] getPassword() {
			javax.swing.JOptionPane.showMessageDialog(
					parent, createDialogContents(), title,
					javax.swing.JOptionPane.QUESTION_MESSAGE);
			return password.getPassword();
		}

		@Override
		protected Object getInputContentsLine() {
			return panel;
		}
	}


	private abstract class Console {
		protected final java.io.Console console;

		protected Console() {
			java.io.Console con = System.console();
			if (con == null) {
				throw new IllegalStateException(String.format(
						"Unable to ask user (%s) via console.", title));
			}
			this.console = con;
		}


		@EnsuresNonNull("#1")
		protected void checkNonNullInput(@Nullable final Object input) throws IOException {
			if(input == null) {
				throw new IOException(String.format(
						"End of input while reading %s", title));
			}
		}
	}


	private class ConsoleRegular extends Console implements RegularInput {
		@Override
		public String getValue() throws IOException {
			String userInput = console.readLine("%s\n%s\n%s", title, message, prompt);
			checkNonNullInput(userInput);
			return userInput;
		}
	}


	private class ConsolePassword extends Console implements PasswordInput {
		@Override
		public char[] getPassword() throws IOException {
			char[] userInput = console.readPassword("%s\n%s\n%s", title, message, prompt);
			checkNonNullInput(userInput);
			return userInput;
		}
	}
	
	private final String title;
	private final String message;
	private final String prompt;


	private UserInput(String title_, String message_, String prompt_) {
		this.title = title_;
		this.message = message_;
		this.prompt = prompt_;
	}
	
	public static String promptUser(String title, String message, String prompt)
			throws IOException, IllegalStateException {
		return Media.selectMedium().createRegularInput(
				new UserInput(title, message, prompt)).getValue();
	}

	public static char[] promptUserPassword(String title, String message, String prompt) 
			throws IOException, IllegalStateException {
		return Media.selectMedium().createPasswordInput(
				new UserInput(title, message, prompt)).getPassword();
	}
}
