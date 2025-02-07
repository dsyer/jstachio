package io.jstach.apt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.Nullable;
import org.junit.Test;

import io.jstach.apt.WhitespaceTokenProcessor.ProcessToken;
import io.jstach.apt.internal.LoggingSupport;
import io.jstach.apt.internal.MustacheToken;
import io.jstach.apt.internal.PositionedToken;
import io.jstach.apt.internal.ProcessingException;
import io.jstach.apt.internal.TokenProcessor;
import io.jstach.apt.internal.token.MustacheTokenizer;

public class WhitespaceTokenProcessorTest {

	PrintStream out = System.out;

	@Test
	public void test() throws ProcessingException, IOException {
		String template = """
				{{<layout.mustache}}
				{{$body}}<span>{{message}}</span>{{/body}}
				{{/layout.mustache}}
				a
				""";

		assertTrue(template.endsWith("\n"));
		var w = new WhitespaceLogger();
		try {
			w.run(NamedReader.ofString(template));
		}
		catch (ProcessingException e) {
			throw new IOException(e.getMessage() + " " + e.position(), e);
		}

	}

	@Test
	public void testDelimiters() throws ProcessingException, IOException {
		String template = """
				{{hello}}
				{{=<% %>=}}
				<% hello %>
				""";

		String actual = run(template);

		String expected = """
				NORMAL TagToken[{{hello}}, VARIABLE]
				NORMAL NewlineToken[newlineChar=LF]
				NORMAL DelimitersToken[delimiters=Delimiters[start1={, start2={, end1=}, end2=}], nextDelimiters=Delimiters[start1=<, start2=%, end1=%, end2=>]]
				IGNORE NewlineToken[newlineChar=LF]
				NORMAL TagToken[<%hello%>, VARIABLE]
				NORMAL NewlineToken[newlineChar=LF]""";
		assertEquals(expected, actual);

	}

	@Test
	public void testSameDelimiters() throws ProcessingException, IOException {
		String template = """
				{{hello}}
				{{=|| ||=}}
				|| hello ||
				""";

		String actual = run(template);

		String expected = """
				NORMAL TagToken[{{hello}}, VARIABLE]
				NORMAL NewlineToken[newlineChar=LF]
				NORMAL DelimitersToken[delimiters=Delimiters[start1={, start2={, end1=}, end2=}], nextDelimiters=Delimiters[start1=|, start2=|, end1=|, end2=|]]
				IGNORE NewlineToken[newlineChar=LF]
				NORMAL TagToken[||hello||, VARIABLE]
				NORMAL NewlineToken[newlineChar=LF]""";
		assertEquals(expected, actual);

	}

	@Test
	public void testSingleDelimiter() throws ProcessingException, IOException {
		String template = """
				{{hello}}
				{{=| |=}}
				| hello |
				|={{ }}=|
				{{ hello }}
				""";

		String actual = run(template);

		String expected = """
				NORMAL TagToken[{{hello}}, VARIABLE]
				NORMAL NewlineToken[newlineChar=LF]
				NORMAL DelimitersToken[delimiters=Delimiters[start1={, start2={, end1=}, end2=}], nextDelimiters=Delimiters[start1=|, start2= , end1=|, end2= ]]
				IGNORE NewlineToken[newlineChar=LF]
				NORMAL TagToken[|hello|, VARIABLE]
				NORMAL NewlineToken[newlineChar=LF]
				NORMAL DelimitersToken[delimiters=Delimiters[start1=|, start2= , end1=|, end2= ], nextDelimiters=Delimiters[start1={, start2={, end1=}, end2=}]]
				IGNORE NewlineToken[newlineChar=LF]
				NORMAL TagToken[{{hello}}, VARIABLE]
				NORMAL NewlineToken[newlineChar=LF]""";
		assertEquals(expected, actual);

	}

	@Test
	public void testWhitespaceInDelimiter() throws ProcessingException, IOException {
		String template = """
				{{hello}}
				{{= | | =}}
				| hello |
				|= {{ }} =|
				{{ hello }}
				""";

		String actual = run(template);

		String expected = """
				NORMAL TagToken[{{hello}}, VARIABLE]
				NORMAL NewlineToken[newlineChar=LF]
				NORMAL DelimitersToken[delimiters=Delimiters[start1={, start2={, end1=}, end2=}], nextDelimiters=Delimiters[start1=|, start2= , end1=|, end2= ]]
				IGNORE NewlineToken[newlineChar=LF]
				NORMAL TagToken[|hello|, VARIABLE]
				NORMAL NewlineToken[newlineChar=LF]
				NORMAL DelimitersToken[delimiters=Delimiters[start1=|, start2= , end1=|, end2= ], nextDelimiters=Delimiters[start1={, start2={, end1=}, end2=}]]
				IGNORE NewlineToken[newlineChar=LF]
				NORMAL TagToken[{{hello}}, VARIABLE]
				NORMAL NewlineToken[newlineChar=LF]""";
		assertEquals(expected, actual);

	}

	@Test
	public void testDelimitersNextToEachOther() throws ProcessingException, IOException {
		String template = """
				{{=[ ]=}}[text]
				""";

		String actual = run(template);

		String expected = """
				NORMAL DelimitersToken[delimiters=Delimiters[start1={, start2={, end1=}, end2=}], nextDelimiters=Delimiters[start1=[, start2= , end1=], end2= ]]
				NORMAL TagToken[[text], VARIABLE]
				NORMAL NewlineToken[newlineChar=LF]""";
		assertEquals(expected, actual);

	}

	private String run(String template) throws IOException {
		assertTrue(template.endsWith("\n"));
		var w = new WhitespaceLogger();
		try {
			w.run(NamedReader.ofString(template));
		}
		catch (ProcessingException e) {
			throw new IOException(e.getMessage() + " " + e.position(), e);
		}
		String actual = printTokens(w.tokens);
		return actual;
	}

	@Test
	public void testEscape() throws ProcessingException, IOException {
		String template = """
				{{{hello}}}
				""";

		String actual = run(template);

		String expected = """
				NORMAL TagToken[{{{hello}}}, UNESCAPED_VARIABLE_THREE_BRACES]
				NORMAL NewlineToken[newlineChar=LF]""";
		assertEquals(expected, actual);

	}

	@Test
	public void testComment() throws Exception {
		String template = """
				Begin.
				{{! Comment Block! }}
				End.
				""";

		String actual = run(template);

		String expected = """
				NORMAL TextToken[text=Begin.]
				NORMAL NewlineToken[newlineChar=LF]
				NORMAL CommentToken[comment= Comment Block! , delimiters=Delimiters[start1={, start2={, end1=}, end2=}]]
				IGNORE NewlineToken[newlineChar=LF]
				NORMAL TextToken[text=End.]
				NORMAL NewlineToken[newlineChar=LF]""";
		assertEquals(expected, actual);
	}

	static String printTokens(List<ProcessToken> tokens) {
		return tokens.stream().map(t -> t.hint() + " " + t.token().innerToken()).collect(Collectors.joining("\n"));
	}

	public static class WhitespaceLogger extends WhitespaceTokenProcessor {

		private final LoggingSupport logging = LoggingSupport.testLogger();

		private List<ProcessToken> tokens = new ArrayList<>();

		public WhitespaceLogger() {
			super();
		}

		public void run(NamedReader reader) throws ProcessingException, IOException {
			TokenProcessor<@Nullable Character> processor = MustacheTokenizer.createInstance(reader.name(), this);
			int readResult;
			while ((readResult = reader.read()) >= 0) {
				try {
					processor.processToken((char) readResult);
				}
				catch (ProcessingException e) {
					if (logging.isDebug()) {
						debug(e.getMessage());
						e.printStackTrace();
					}
					throw e;
				}
			}
			processor.processToken(EOF);
		}

		@Override
		public LoggingSupport logging() {
			return logging;
		}

		@Override
		protected void processTokenGroup(List<ProcessToken> tokens) throws ProcessingException {
			debug("tokens: " + tokens.stream().map(t -> t.hint() + " " + t.token().innerToken())
					.collect(Collectors.joining(", ")));
			this.tokens.addAll(tokens);
			super.processTokenGroup(tokens);
		}

		@Override
		protected void handleToken(PositionedToken<MustacheToken> positionedToken) throws ProcessingException {
			// System.out.println(positionedToken.innerToken());

		}

	}

}
