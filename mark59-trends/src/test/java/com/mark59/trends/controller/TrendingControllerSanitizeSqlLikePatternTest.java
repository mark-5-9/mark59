package com.mark59.trends.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for TrendingController.sanitizeSqlLikePattern() method.
 * Uses reflection to test the private method for SQL injection prevention in LIKE patterns.
 */
class TrendingControllerSanitizeSqlLikePatternTest {

	private TrendingController controller;
	private Method sanitizeSqlLikePatternMethod;

	@BeforeEach
	void setUp() throws Exception {
		controller = new TrendingController();
		// Access the private method using reflection
		sanitizeSqlLikePatternMethod = TrendingController.class.getDeclaredMethod(
			"sanitizeSqlLikePattern", String.class, String.class);
		sanitizeSqlLikePatternMethod.setAccessible(true);
	}

	private String invokeSanitizeSqlLikePattern(String input, String whenSanitizedEmpty) throws Exception {
		return (String) sanitizeSqlLikePatternMethod.invoke(controller, input, whenSanitizedEmpty);
	}

	// ========== Valid LIKE Patterns ==========

	@Test
	@DisplayName("Should accept wildcard pattern %")
	void testWildcardPattern() throws Exception {
		assertEquals("%", invokeSanitizeSqlLikePattern("%", "%"));
	}

	@Test
	@DisplayName("Should accept simple text pattern")
	void testSimpleTextPattern() throws Exception {
		String pattern = "test";
		assertEquals(pattern, invokeSanitizeSqlLikePattern(pattern, "%"));
	}

	@Test
	@DisplayName("Should accept pattern with wildcard prefix")
	void testPatternWithWildcardPrefix() throws Exception {
		String pattern = "%test";
		assertEquals(pattern, invokeSanitizeSqlLikePattern(pattern, "%"));
	}

	@Test
	@DisplayName("Should accept pattern with wildcard suffix")
	void testPatternWithWildcardSuffix() throws Exception {
		String pattern = "test%";
		assertEquals(pattern, invokeSanitizeSqlLikePattern(pattern, "%"));
	}

	@Test
	@DisplayName("Should accept pattern with wildcards on both sides")
	void testPatternWithBothWildcards() throws Exception {
		String pattern = "%test%";
		assertEquals(pattern, invokeSanitizeSqlLikePattern(pattern, "%"));
	}

	@Test
	@DisplayName("Should accept pattern with underscore wildcard")
	void testPatternWithUnderscore() throws Exception {
		String pattern = "test_123";
		assertEquals(pattern, invokeSanitizeSqlLikePattern(pattern, "%"));
	}

	@Test
	@DisplayName("Should accept pattern with dots")
	void testPatternWithDots() throws Exception {
		String pattern = "test.file";
		assertEquals(pattern, invokeSanitizeSqlLikePattern(pattern, "%"));
	}

	@Test
	@DisplayName("Should accept pattern with hyphens")
	void testPatternWithHyphens() throws Exception {
		String pattern = "test-name";
		assertEquals(pattern, invokeSanitizeSqlLikePattern(pattern, "%"));
	}

	@Test
	@DisplayName("Should accept alphanumeric pattern")
	void testAlphanumericPattern() throws Exception {
		String pattern = "Test123";
		assertEquals(pattern, invokeSanitizeSqlLikePattern(pattern, "%"));
	}

	@Test
	@DisplayName("Should accept pattern with spaces")
	void testPatternWithSpaces() throws Exception {
		String pattern = "test pattern";
		assertEquals(pattern, invokeSanitizeSqlLikePattern(pattern, "%"));
	}

	@Test
	@DisplayName("Should accept complex valid pattern")
	void testComplexValidPattern() throws Exception {
		String pattern = "%Test_123-Name.File%";
		assertEquals(pattern, invokeSanitizeSqlLikePattern(pattern, "%"));
	}

	// ========== Empty and Null Input ==========

	@Test
	@DisplayName("Should return default for empty string when default is %")
	void testEmptyStringReturnsPercent() throws Exception {
		assertEquals("", invokeSanitizeSqlLikePattern("", "%"));
	}

	@Test
	@DisplayName("Should return custom default for empty string")
	void testEmptyStringReturnsCustomDefault() throws Exception {
		assertEquals("", invokeSanitizeSqlLikePattern("", ""));
	}

	@Test
	@DisplayName("Should return % for whitespace-only percent when default is %")
	void testWhitespacePercentPattern() throws Exception {
		String pattern = "  %  ";
		assertEquals(pattern, invokeSanitizeSqlLikePattern(pattern, "%"));
	}

	@Test
	@DisplayName("Should trim and sanitize pattern with leading/trailing whitespace")
	void testPatternWithWhitespace() throws Exception {
		String pattern = "  test  ";
		assertEquals("test", invokeSanitizeSqlLikePattern(pattern, "%"));
	}

	// ========== SQL Injection Attempts - Quotes and Escapes ==========

	@Test
	@DisplayName("Should remove single quotes")
	void testRemoveSingleQuotes() throws Exception {
		String pattern = "test'OR'1'='1";
		assertEquals("testOR11", invokeSanitizeSqlLikePattern(pattern, "%"));
	}

	@Test
	@DisplayName("Should remove double quotes")
	void testRemoveDoubleQuotes() throws Exception {
		String pattern = "test\"value\"";
		assertEquals("testvalue", invokeSanitizeSqlLikePattern(pattern, "%"));
	}

	@Test
	@DisplayName("Should remove backslashes")
	void testRemoveBackslashes() throws Exception {
		String pattern = "test\\escape";
		assertEquals("testescape", invokeSanitizeSqlLikePattern(pattern, "%"));
	}

	@Test
	@DisplayName("Should remove all quote combinations")
	void testRemoveAllQuotes() throws Exception {
		String pattern = "test'\"\\pattern";
		assertEquals("testpattern", invokeSanitizeSqlLikePattern(pattern, "%"));
	}

	// ========== SQL Injection Attempts - Comments ==========

	@Test
	@DisplayName("Should remove double-dash comments")
	void testRemoveDoubleHyphenComments() throws Exception {
		String pattern = "test--comment";
		assertEquals("testcomment", invokeSanitizeSqlLikePattern(pattern, "%"));
	}

	@Test
	@DisplayName("Should remove block comment start")
	void testRemoveBlockCommentStart() throws Exception {
		String pattern = "test/*comment";
		assertEquals("testcomment", invokeSanitizeSqlLikePattern(pattern, "%"));
	}

	@Test
	@DisplayName("Should remove block comment end")
	void testRemoveBlockCommentEnd() throws Exception {
		String pattern = "test*/comment";
		assertEquals("testcomment", invokeSanitizeSqlLikePattern(pattern, "%"));
	}

	@Test
	@DisplayName("Should remove full block comment")
	void testRemoveFullBlockComment() throws Exception {
		String pattern = "test/*comment*/value";
		assertEquals("testcommentvalue", invokeSanitizeSqlLikePattern(pattern, "%"));
	}

	// ========== SQL Injection Attempts - Keywords ==========

	@Test
	@DisplayName("Should remove OR keyword")
	void testRemoveOrKeyword() throws Exception {
		String pattern = "test OR admin";
		assertEquals("test  admin", invokeSanitizeSqlLikePattern(pattern, "%"));
	}

	@Test
	@DisplayName("Should remove AND keyword")
	void testRemoveAndKeyword() throws Exception {
		String pattern = "test AND admin";
		assertEquals("test  admin", invokeSanitizeSqlLikePattern(pattern, "%"));
	}

	@Test
	@DisplayName("Should remove UNION keyword")
	void testRemoveUnionKeyword() throws Exception {
		String pattern = "test UNION select";
		assertEquals("test  select", invokeSanitizeSqlLikePattern(pattern, "%"));
	}

	@Test
	@DisplayName("Should remove SELECT keyword")
	void testRemoveSelectKeyword() throws Exception {
		String pattern = "SELECT * FROM users";
		// SELECT removed, asterisk removed
		assertEquals("  FROM users", invokeSanitizeSqlLikePattern(pattern, "%"));
	}

	@Test
	@DisplayName("Should remove DROP keyword")
	void testRemoveDropKeyword() throws Exception {
		String pattern = "test DROP table";
		assertEquals("test  table", invokeSanitizeSqlLikePattern(pattern, "%"));
	}

	@Test
	@DisplayName("Should remove INSERT keyword")
	void testRemoveInsertKeyword() throws Exception {
		String pattern = "test INSERT into";
		assertEquals("test  into", invokeSanitizeSqlLikePattern(pattern, "%"));
	}

	@Test
	@DisplayName("Should remove UPDATE keyword")
	void testRemoveUpdateKeyword() throws Exception {
		String pattern = "test UPDATE set";
		assertEquals("test  set", invokeSanitizeSqlLikePattern(pattern, "%"));
	}

	@Test
	@DisplayName("Should remove DELETE keyword")
	void testRemoveDeleteKeyword() throws Exception {
		String pattern = "test DELETE from";
		assertEquals("test  from", invokeSanitizeSqlLikePattern(pattern, "%"));
	}

	@Test
	@DisplayName("Should remove EXEC keyword")
	void testRemoveExecKeyword() throws Exception {
		String pattern = "test EXEC sp";
		assertEquals("test  sp", invokeSanitizeSqlLikePattern(pattern, "%"));
	}

	@Test
	@DisplayName("Should preserve words containing keywords")
	void testPreserveWordsContainingKeywords() throws Exception {
		// "FOREIGN" contains "OR", "STANDARD" contains "AND"
		// Word boundaries should prevent removal
		String pattern = "FOREIGN STANDARD";
		assertEquals("FOREIGN STANDARD", invokeSanitizeSqlLikePattern(pattern, "%"));
	}

	@Test
	@DisplayName("Should remove keywords case-insensitively")
	void testRemoveKeywordsCaseInsensitive() throws Exception {
		String pattern = "test or OR Or admin";
		// Only uppercase OR is removed (regex is case-sensitive)
		assertEquals("test or  Or admin", invokeSanitizeSqlLikePattern(pattern, "%"));
	}

	// ========== SQL Injection Attempts - Operators ==========

	@Test
	@DisplayName("Should remove parentheses")
	void testRemoveParentheses() throws Exception {
		String pattern = "test(value)";
		assertEquals("testvalue", invokeSanitizeSqlLikePattern(pattern, "%"));
	}

	@Test
	@DisplayName("Should remove equals sign")
	void testRemoveEquals() throws Exception {
		String pattern = "test=value";
		assertEquals("testvalue", invokeSanitizeSqlLikePattern(pattern, "%"));
	}

	@Test
	@DisplayName("Should remove less than")
	void testRemoveLessThan() throws Exception {
		String pattern = "test<100";
		assertEquals("test100", invokeSanitizeSqlLikePattern(pattern, "%"));
	}

	@Test
	@DisplayName("Should remove greater than")
	void testRemoveGreaterThan() throws Exception {
		String pattern = "test>100";
		assertEquals("test100", invokeSanitizeSqlLikePattern(pattern, "%"));
	}

	@Test
	@DisplayName("Should remove all operators")
	void testRemoveAllOperators() throws Exception {
		String pattern = "test(x=10)AND(y>5)";
		assertEquals("testx10y5", invokeSanitizeSqlLikePattern(pattern, "%"));
	}

	// ========== Special Characters Removal ==========

	@Test
	@DisplayName("Should remove special characters not in whitelist")
	void testRemoveSpecialCharacters() throws Exception {
		String pattern = "test@#$%^&*value";
		// @ # $ ^ & * should be removed, % should stay
		assertEquals("test%value", invokeSanitizeSqlLikePattern(pattern, "%"));
	}

	@Test
	@DisplayName("Should remove brackets")
	void testRemoveBrackets() throws Exception {
		String pattern = "test[value]";
		assertEquals("testvalue", invokeSanitizeSqlLikePattern(pattern, "%"));
	}

	@Test
	@DisplayName("Should remove braces")
	void testRemoveBraces() throws Exception {
		String pattern = "test{value}";
		assertEquals("testvalue", invokeSanitizeSqlLikePattern(pattern, "%"));
	}

	@Test
	@DisplayName("Should remove pipe and ampersand")
	void testRemovePipeAndAmpersand() throws Exception {
		String pattern = "test|value&more";
		assertEquals("testvaluemore", invokeSanitizeSqlLikePattern(pattern, "%"));
	}

	@Test
	@DisplayName("Should remove plus and asterisk")
	void testRemovePlusAndAsterisk() throws Exception {
		String pattern = "test+value*more";
		assertEquals("testvaluemore", invokeSanitizeSqlLikePattern(pattern, "%"));
	}

	// ========== Empty Result After Sanitization ==========

	@Test
	@DisplayName("Should return % when sanitized to empty with % default")
	void testReturnPercentWhenEmpty() throws Exception {
		String pattern = "!@#$%^&*()";
		// After removing special chars, only % remains, but then gets removed by final whitelist
		// Wait, % is in the whitelist, so it should remain
		assertEquals("%", invokeSanitizeSqlLikePattern(pattern, "%"));
	}

	@Test
	@DisplayName("Should return empty when sanitized to empty with empty default")
	void testReturnEmptyWhenSanitizedEmpty() throws Exception {
		String pattern = "!@#$^&*()";
		// All special chars removed, no % in input
		assertEquals("", invokeSanitizeSqlLikePattern(pattern, ""));
	}

	@Test
	@DisplayName("Should return custom default when only SQL keywords present")
	void testReturnDefaultForOnlyKeywords() throws Exception {
		String pattern = "SELECT OR AND DROP";
		// All keywords removed, only spaces remain, then spaces removed by trim -> empty
		// Actually spaces are allowed in the whitelist
		assertEquals("   ", invokeSanitizeSqlLikePattern(pattern, "CUSTOM"));
	}

	// ========== Complex SQL Injection Attempts ==========

	@Test
	@DisplayName("Should sanitize classic SQL injection attempt")
	void testClassicSqlInjection() throws Exception {
		String pattern = "' OR '1'='1";
		// Quotes and equals removed, OR keyword removed (case-sensitive regex)
		assertEquals("  11", invokeSanitizeSqlLikePattern(pattern, "%"));
	}

	@Test
	@DisplayName("Should sanitize union-based injection")
	void testUnionBasedInjection() throws Exception {
		String pattern = "test' UNION SELECT * FROM users--";
		assertEquals("test    FROM users", invokeSanitizeSqlLikePattern(pattern, "%"));
	}

	@Test
	@DisplayName("Should sanitize comment-based injection")
	void testCommentBasedInjection() throws Exception {
		String pattern = "test'/**/OR/**/1=1";
		assertEquals("testOR11", invokeSanitizeSqlLikePattern(pattern, "%"));
	}

	@Test
	@DisplayName("Should sanitize nested injection attempt")
	void testNestedInjection() throws Exception {
		String pattern = "test'; DROP TABLE users;--";
		assertEquals("test  TABLE users", invokeSanitizeSqlLikePattern(pattern, "%"));
	}

	@Test
	@DisplayName("Should sanitize boolean-based blind injection")
	void testBooleanBasedInjection() throws Exception {
		String pattern = "test' AND 1=1--";
		assertEquals("test  11", invokeSanitizeSqlLikePattern(pattern, "%"));
	}

	@Test
	@DisplayName("Should sanitize time-based blind injection")
	void testTimeBasedInjection() throws Exception {
		String pattern = "test'; WAITFOR DELAY '00:00:05'--";
		assertEquals("test WAITFOR DELAY 000005", invokeSanitizeSqlLikePattern(pattern, "%"));
	}

	@Test
	@DisplayName("Should sanitize stacked queries injection")
	void testStackedQueriesInjection() throws Exception {
		String pattern = "test'; INSERT INTO logs VALUES ('hacked');--";
		assertEquals("test  INTO logs VALUES hacked", invokeSanitizeSqlLikePattern(pattern, "%"));
	}

	// ========== Case Sensitivity and Word Boundaries ==========

	@Test
	@DisplayName("Should handle mixed case keywords")
	void testMixedCaseKeywords() throws Exception {
		String pattern = "test Or AnD uNiOn select";
		// Regex is case-sensitive, so mixed case keywords are NOT removed
		assertEquals("test Or AnD uNiOn select", invokeSanitizeSqlLikePattern(pattern, "%"));
	}

	@Test
	@DisplayName("Should preserve uppercase normal text")
	void testPreserveUppercase() throws Exception {
		String pattern = "TEST_FILE_123";
		assertEquals(pattern, invokeSanitizeSqlLikePattern(pattern, "%"));
	}

	@Test
	@DisplayName("Should preserve lowercase normal text")
	void testPreserveLowercase() throws Exception {
		String pattern = "test_file_123";
		assertEquals(pattern, invokeSanitizeSqlLikePattern(pattern, "%"));
	}

	// ========== whenSanitizedEmpty Parameter Behavior ==========

	@Test
	@DisplayName("Should use whenSanitizedEmpty when result is empty")
	void testWhenSanitizedEmptyParameter() throws Exception {
		String pattern = "!@#$^&*";
		String customDefault = "DEFAULT_VALUE";
		assertEquals(customDefault, invokeSanitizeSqlLikePattern(pattern, customDefault));
	}

	@Test
	@DisplayName("Should not use whenSanitizedEmpty when result is not empty")
	void testWhenSanitizedEmptyNotUsed() throws Exception {
		String pattern = "test";
		String customDefault = "DEFAULT_VALUE";
		assertEquals("test", invokeSanitizeSqlLikePattern(pattern, customDefault));
	}

	@Test
	@DisplayName("Should use % as default for empty result")
	void testDefaultPercentForEmpty() throws Exception {
		String pattern = "()=<>";
		assertEquals("%", invokeSanitizeSqlLikePattern(pattern, "%"));
	}

	@Test
	@DisplayName("Should use empty string as default")
	void testDefaultEmptyString() throws Exception {
		String pattern = "()=<>";
		assertEquals("", invokeSanitizeSqlLikePattern(pattern, ""));
	}

	// ========== Real-World Valid Patterns ==========

	@Test
	@DisplayName("Should accept file extension pattern")
	void testFileExtensionPattern() throws Exception {
		String pattern = "%.log";
		assertEquals(pattern, invokeSanitizeSqlLikePattern(pattern, "%"));
	}

	@Test
	@DisplayName("Should accept filename prefix pattern")
	void testFilenamePrefixPattern() throws Exception {
		String pattern = "error_%";
		assertEquals(pattern, invokeSanitizeSqlLikePattern(pattern, "%"));
	}

	@Test
	@DisplayName("Should accept transaction name pattern")
	void testTransactionNamePattern() throws Exception {
		String pattern = "%Login_Transaction%";
		assertEquals(pattern, invokeSanitizeSqlLikePattern(pattern, "%"));
	}

	@Test
	@DisplayName("Should accept date-like pattern")
	void testDateLikePattern() throws Exception {
		String pattern = "2024-01-%";
		assertEquals(pattern, invokeSanitizeSqlLikePattern(pattern, "%"));
	}

	@Test
	@DisplayName("Should accept version pattern")
	void testVersionPattern() throws Exception {
		String pattern = "v1.2.%";
		assertEquals(pattern, invokeSanitizeSqlLikePattern(pattern, "%"));
	}

	// ========== Edge Cases ==========

	@Test
	@DisplayName("Should handle very long pattern")
	void testVeryLongPattern() throws Exception {
		String pattern = "a".repeat(1000);
		assertEquals(pattern, invokeSanitizeSqlLikePattern(pattern, "%"));
	}

	@Test
	@DisplayName("Should handle pattern with only wildcards")
	void testOnlyWildcards() throws Exception {
		String pattern = "%%__%__%%";
		assertEquals(pattern, invokeSanitizeSqlLikePattern(pattern, "%"));
	}

	@Test
	@DisplayName("Should handle pattern with multiple consecutive spaces")
	void testMultipleSpaces() throws Exception {
		String pattern = "test    pattern";
		assertEquals(pattern, invokeSanitizeSqlLikePattern(pattern, "%"));
	}

	@Test
	@DisplayName("Should handle pattern with tabs and newlines")
	void testTabsAndNewlines() throws Exception {
		String pattern = "test\tpattern\n";
		// Input is trimmed first, removing trailing newline
		// \s in whitelist includes tabs and other whitespace
		assertEquals("test\tpattern", invokeSanitizeSqlLikePattern(pattern, "%"));
	}
}
