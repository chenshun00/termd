package io.termd.core.readline;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class QuotingTest {

  @Test
  public void testFoo() {
    assertEscape("a", "a");
    assertEscape("\n", "\n");
    assertEscape("a\n", "a\n");
    assertEscape("\na", "\na");
  }

  @Test
  public void testQuote() {
    assertEscape("'", "<'>");
    assertEscape("'a", "<'>a");
    assertEscape("'a'", "<'>a</'>");
    assertEscape("'\"'", "<'>\"</'>");
    assertEscape("'\n'", "<'>\n</'>");
    assertEscape("'\\'", "<'>\\</'>");
    assertEscape("'a\nb'", "<'>a\nb</'>");
    assertEscape("'a'\n", "<'>a</'>\n");
  }

  @Test
  public void testDoubleQuote() {
    assertEscape("\"", "<\">");
    assertEscape("\"a", "<\">a");
    assertEscape("\"a\"", "<\">a</\">");
    assertEscape("\"'\"", "<\">'</\">");
    assertEscape("\"\n\"", "<\">\n</\">");
    assertEscape("\"\\\"", "<\">\\</\">");
    assertEscape("\"a\nb\"", "<\">a\nb</\">");
    assertEscape("\"a\"\n", "<\">a</\">\n");
  }

  @Test
  public void testBackslash() {
    assertEscape("\\", "[");
    assertEscape("\\a", "[a]");
    assertEscape("\\ab", "[a]b");
    assertEscape("\\\\", "[\\]");
    assertEscape("\\'", "[']");
    assertEscape("\\\"", "[\"]");
    assertEscape("\\\n", "[\n]");
  }

  private void assertEscape(String line, String expected) {
    String actual = escape(line);
    assertEquals(expected, actual);
  }

  private String escape(String line) {
    final StringBuilder builder = new StringBuilder();
    QuoteFilter escaper = new QuoteFilter(new Quoter() {
      Quoting quoting;
      @Override
      public void quotingChanged(Quoting prev, Quoting next) {
        switch (next) {
          case NONE:
            if (this.quoting == Quoting.ESC) {
              builder.append(']');
            } else {
              builder.append("</").appendCodePoint(this.quoting.ch).append(">");
            }
            break;
          case ESC:
            builder.append("[");
            break;
          default:
            builder.append("<").appendCodePoint(next.ch).append(">");
        }
        this.quoting = next;
      }
      @Override
      public void accept(int value) {
        builder.appendCodePoint(value);
      }
    });
    for (int offset = 0;offset < line.length();) {
      int cp = line.codePointAt(offset);
      escaper.accept(cp);
      offset += Character.charCount(cp);
    }
    return builder.toString();
  }

}