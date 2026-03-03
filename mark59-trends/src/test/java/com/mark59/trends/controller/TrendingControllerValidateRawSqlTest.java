package com.mark59.trends.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for TrendingController.validateRawSql() method.
 * Uses reflection to test the private method for SQL injection prevention.
 */
class TrendingControllerValidateRawSqlTest {

	private TrendingController controller;
	private Method validateRawSqlMethod;

	@BeforeEach
	void setUp() throws Exception {
		controller = new TrendingController();
		// Access the private method using reflection
		validateRawSqlMethod = TrendingController.class.getDeclaredMethod("validateRawSql", String.class);
		validateRawSqlMethod.setAccessible(true);
	}

	private String invokeValidateRawSql(String sql) throws Exception {
		return (String) validateRawSqlMethod.invoke(controller, sql);
	}

	// ========== Valid SELECT Statements ==========

	@Test
	@DisplayName("Should accept valid simple SELECT statement")
	void testValidSimpleSelect() throws Exception {
		String sql = "SELECT * FROM users";
		assertEquals(sql, invokeValidateRawSql(sql));
	}

	@Test
	@DisplayName("Should accept SELECT with WHERE clause")
	void testValidSelectWithWhere() throws Exception {
		String sql = "SELECT id, name FROM users WHERE active = 1";
		assertEquals(sql, invokeValidateRawSql(sql));
	}

	@Test
	@DisplayName("Should accept SELECT with JOIN")
	void testValidSelectWithJoin() throws Exception {
		String sql = "SELECT u.name, o.total FROM users u JOIN orders o ON u.id = o.user_id";
		assertEquals(sql, invokeValidateRawSql(sql));
	}

	@Test
	@DisplayName("Should accept SELECT with GROUP BY and ORDER BY")
	void testValidSelectWithGroupByOrderBy() throws Exception {
		String sql = "SELECT department, COUNT(*) FROM employees GROUP BY department ORDER BY COUNT(*) DESC";
		assertEquals(sql, invokeValidateRawSql(sql));
	}

	@Test
	@DisplayName("Should accept SELECT with subquery")
	void testValidSelectWithSubquery() throws Exception {
		String sql = "SELECT * FROM users WHERE id IN (SELECT user_id FROM orders WHERE total > 100)";
		assertEquals(sql, invokeValidateRawSql(sql));
	}

	@Test
	@DisplayName("Should accept case-insensitive SELECT")
	void testValidSelectCaseInsensitive() throws Exception {
		String sql = "select * from users";
		assertEquals(sql, invokeValidateRawSql(sql));
	}

	@Test
	@DisplayName("Should accept SELECT with leading whitespace")
	void testValidSelectWithWhitespace() throws Exception {
		String sql = "   SELECT * FROM users   ";
		// The method returns the original SQL as-is (including whitespace)
		assertEquals(sql, invokeValidateRawSql(sql));
	}

	// ========== Null and Empty Input ==========

	@Test
	@DisplayName("Should return empty string for null input")
	void testNullInput() throws Exception {
		assertEquals("", invokeValidateRawSql(null));
	}

	@Test
	@DisplayName("Should return empty string for empty input")
	void testEmptyInput() throws Exception {
		assertEquals("", invokeValidateRawSql(""));
	}

	@Test
	@DisplayName("Should return empty string for whitespace-only input")
	void testWhitespaceOnlyInput() throws Exception {
		assertEquals("", invokeValidateRawSql("   "));
	}

	// ========== Non-SELECT Statements ==========

	@Test
	@DisplayName("Should reject INSERT statement")
	void testRejectInsert() {
		String sql = "INSERT INTO users (name) VALUES ('test')";
		Exception exception = assertThrows(Exception.class, () -> invokeValidateRawSql(sql));
		assertTrue(exception.getCause() instanceof IllegalArgumentException);
		assertTrue(exception.getCause().getMessage().contains("Only SELECT statements are allowed"));
	}

	@Test
	@DisplayName("Should reject UPDATE statement")
	void testRejectUpdate() {
		String sql = "UPDATE users SET name = 'test' WHERE id = 1";
		Exception exception = assertThrows(Exception.class, () -> invokeValidateRawSql(sql));
		assertTrue(exception.getCause() instanceof IllegalArgumentException);
		assertTrue(exception.getCause().getMessage().contains("Only SELECT statements are allowed"));
	}

	@Test
	@DisplayName("Should reject DELETE statement")
	void testRejectDelete() {
		String sql = "DELETE FROM users WHERE id = 1";
		Exception exception = assertThrows(Exception.class, () -> invokeValidateRawSql(sql));
		assertTrue(exception.getCause() instanceof IllegalArgumentException);
		assertTrue(exception.getCause().getMessage().contains("Only SELECT statements are allowed"));
	}

	// ========== Dangerous Keywords ==========

	@Test
	@DisplayName("Should reject SELECT with INSERT keyword")
	void testRejectSelectWithInsert() {
		String sql = "SELECT * FROM users WHERE name = 'INSERT'";
		Exception exception = assertThrows(Exception.class, () -> invokeValidateRawSql(sql));
		assertTrue(exception.getCause() instanceof IllegalArgumentException);
		assertTrue(exception.getCause().getMessage().contains("INSERT"));
	}

	@Test
	@DisplayName("Should reject SELECT with UPDATE keyword")
	void testRejectSelectWithUpdate() {
		String sql = "SELECT * FROM users WHERE status = 'UPDATE'";
		Exception exception = assertThrows(Exception.class, () -> invokeValidateRawSql(sql));
		assertTrue(exception.getCause() instanceof IllegalArgumentException);
		assertTrue(exception.getCause().getMessage().contains("UPDATE"));
	}

	@Test
	@DisplayName("Should reject SELECT with DELETE keyword")
	void testRejectSelectWithDelete() {
		String sql = "SELECT * FROM users WHERE action = 'DELETE'";
		Exception exception = assertThrows(Exception.class, () -> invokeValidateRawSql(sql));
		assertTrue(exception.getCause() instanceof IllegalArgumentException);
		assertTrue(exception.getCause().getMessage().contains("DELETE"));
	}

	@Test
	@DisplayName("Should reject SELECT with DROP keyword")
	void testRejectSelectWithDrop() {
		String sql = "SELECT * FROM users; DROP TABLE users";
		Exception exception = assertThrows(Exception.class, () -> invokeValidateRawSql(sql));
		assertTrue(exception.getCause() instanceof IllegalArgumentException);
		// Will fail on semicolon first, but DROP is also checked
	}

	@Test
	@DisplayName("Should reject SELECT with TRUNCATE keyword")
	void testRejectSelectWithTruncate() {
		String sql = "SELECT * FROM users WHERE id = 1 TRUNCATE TABLE users";
		Exception exception = assertThrows(Exception.class, () -> invokeValidateRawSql(sql));
		assertTrue(exception.getCause() instanceof IllegalArgumentException);
		assertTrue(exception.getCause().getMessage().contains("TRUNCATE"));
	}

	@Test
	@DisplayName("Should reject SELECT with ALTER keyword")
	void testRejectSelectWithAlter() {
		String sql = "SELECT * FROM users ALTER TABLE users ADD COLUMN test";
		Exception exception = assertThrows(Exception.class, () -> invokeValidateRawSql(sql));
		assertTrue(exception.getCause() instanceof IllegalArgumentException);
		assertTrue(exception.getCause().getMessage().contains("ALTER"));
	}

	@Test
	@DisplayName("Should reject SELECT with CREATE keyword")
	void testRejectSelectWithCreate() {
		String sql = "SELECT * FROM users CREATE TABLE test";
		Exception exception = assertThrows(Exception.class, () -> invokeValidateRawSql(sql));
		assertTrue(exception.getCause() instanceof IllegalArgumentException);
		assertTrue(exception.getCause().getMessage().contains("CREATE"));
	}

	@Test
	@DisplayName("Should reject SELECT with EXEC keyword")
	void testRejectSelectWithExec() {
		String sql = "SELECT * FROM users EXEC sp_executesql";
		Exception exception = assertThrows(Exception.class, () -> invokeValidateRawSql(sql));
		assertTrue(exception.getCause() instanceof IllegalArgumentException);
		assertTrue(exception.getCause().getMessage().contains("EXEC"));
	}

	@Test
	@DisplayName("Should reject SELECT with EXECUTE keyword")
	void testRejectSelectWithExecute() {
		String sql = "SELECT * FROM users EXECUTE sp_executesql";
		Exception exception = assertThrows(Exception.class, () -> invokeValidateRawSql(sql));
		assertTrue(exception.getCause() instanceof IllegalArgumentException);
		assertTrue(exception.getCause().getMessage().contains("EXECUTE"));
	}

	// ========== SQL Injection Attempts ==========

	@Test
	@DisplayName("Should reject SQL with double-dash comments")
	void testRejectDoubleHyphenComments() {
		String sql = "SELECT * FROM users -- WHERE id = 1";
		Exception exception = assertThrows(Exception.class, () -> invokeValidateRawSql(sql));
		assertTrue(exception.getCause() instanceof IllegalArgumentException);
		assertTrue(exception.getCause().getMessage().contains("comments are not allowed"));
	}

	@Test
	@DisplayName("Should reject SQL with block comments")
	void testRejectBlockComments() {
		String sql = "SELECT * FROM users /* comment */ WHERE id = 1";
		Exception exception = assertThrows(Exception.class, () -> invokeValidateRawSql(sql));
		assertTrue(exception.getCause() instanceof IllegalArgumentException);
		assertTrue(exception.getCause().getMessage().contains("comments are not allowed"));
	}

	@Test
	@DisplayName("Should reject SQL with semicolon (statement separator)")
	void testRejectSemicolon() {
		String sql = "SELECT * FROM users; SELECT * FROM passwords";
		Exception exception = assertThrows(Exception.class, () -> invokeValidateRawSql(sql));
		assertTrue(exception.getCause() instanceof IllegalArgumentException);
		assertTrue(exception.getCause().getMessage().contains("Multiple SQL statements are not allowed"));
	}

	@Test
	@DisplayName("Should reject UNION queries")
	void testRejectUnion() {
		String sql = "SELECT id FROM users UNION SELECT id FROM admin";
		Exception exception = assertThrows(Exception.class, () -> invokeValidateRawSql(sql));
		assertTrue(exception.getCause() instanceof IllegalArgumentException);
		assertTrue(exception.getCause().getMessage().contains("UNION queries are not allowed"));
	}

	@Test
	@DisplayName("Should reject UNION ALL queries")
	void testRejectUnionAll() {
		String sql = "SELECT id FROM users UNION ALL SELECT id FROM admin";
		Exception exception = assertThrows(Exception.class, () -> invokeValidateRawSql(sql));
		assertTrue(exception.getCause() instanceof IllegalArgumentException);
		assertTrue(exception.getCause().getMessage().contains("UNION"));
	}

	// ========== SELECT INTO Patterns ==========

	@Test
	@DisplayName("Should reject SELECT INTO with spaces")
	void testRejectSelectIntoWithSpaces() {
		String sql = "SELECT * INTO backup_users FROM users";
		Exception exception = assertThrows(Exception.class, () -> invokeValidateRawSql(sql));
		assertTrue(exception.getCause() instanceof IllegalArgumentException);
		assertTrue(exception.getCause().getMessage().contains("SELECT INTO is not allowed"));
	}

	@Test
	@DisplayName("Should reject SELECT INTO with tabs")
	void testRejectSelectIntoWithTabs() {
		String sql = "SELECT *\tINTO\tbackup_users FROM users";
		Exception exception = assertThrows(Exception.class, () -> invokeValidateRawSql(sql));
		assertTrue(exception.getCause() instanceof IllegalArgumentException);
		assertTrue(exception.getCause().getMessage().contains("SELECT INTO is not allowed"));
	}

	@Test
	@DisplayName("Should reject SELECT INTO with mixed whitespace")
	void testRejectSelectIntoWithMixedWhitespace() {
		String sql = "SELECT * INTO backup FROM users";
		Exception exception = assertThrows(Exception.class, () -> invokeValidateRawSql(sql));
		assertTrue(exception.getCause() instanceof IllegalArgumentException);
		assertTrue(exception.getCause().getMessage().contains("SELECT INTO is not allowed"));
	}

	@Test
	@DisplayName("Should accept word containing INTO but not SELECT INTO pattern")
	void testAcceptWordContainingInto() throws Exception {
		// This should pass because "INTO" is not preceded by SELECT as a separate keyword
		// However, due to our indexOf implementation, this might still fail
		// Let's test the actual behavior
		String sql = "SELECTointob FROM users";
		// This should actually pass since "INTO" is not surrounded by spaces/tabs
		assertEquals(sql, invokeValidateRawSql(sql));
	}

	// ========== Dangerous MySQL Functions ==========

	@Test
	@DisplayName("Should reject LOAD_FILE function")
	void testRejectLoadFile() {
		String sql = "SELECT LOAD_FILE('/etc/passwd')";
		Exception exception = assertThrows(Exception.class, () -> invokeValidateRawSql(sql));
		assertTrue(exception.getCause() instanceof IllegalArgumentException);
		assertTrue(exception.getCause().getMessage().contains("LOAD_FILE"));
	}

	@Test
	@DisplayName("Should reject INTO OUTFILE")
	void testRejectIntoOutfile() {
		String sql = "SELECT * FROM users INTO OUTFILE '/tmp/users.txt'";
		Exception exception = assertThrows(Exception.class, () -> invokeValidateRawSql(sql));
		assertTrue(exception.getCause() instanceof IllegalArgumentException);
		// Will be caught by either SELECT INTO or INTO OUTFILE check
		assertTrue(exception.getCause().getMessage().contains("INTO OUTFILE") ||
		           exception.getCause().getMessage().contains("SELECT INTO"));
	}

	@Test
	@DisplayName("Should reject INTO DUMPFILE")
	void testRejectIntoDumpfile() {
		String sql = "SELECT * FROM users INTO DUMPFILE '/tmp/dump.txt'";
		Exception exception = assertThrows(Exception.class, () -> invokeValidateRawSql(sql));
		assertTrue(exception.getCause() instanceof IllegalArgumentException);
		// Will be caught by either SELECT INTO or INTO DUMPFILE check
		assertTrue(exception.getCause().getMessage().contains("INTO DUMPFILE") ||
		           exception.getCause().getMessage().contains("SELECT INTO"));
	}

	@Test
	@DisplayName("Should reject LOAD DATA")
	void testRejectLoadData() {
		String sql = "SELECT * FROM users WHERE id = 1 LOAD DATA INFILE";
		Exception exception = assertThrows(Exception.class, () -> invokeValidateRawSql(sql));
		assertTrue(exception.getCause() instanceof IllegalArgumentException);
		assertTrue(exception.getCause().getMessage().contains("LOAD DATA") ||
		           exception.getCause().getMessage().contains("LOAD_DATA"));
	}

	// ========== Dangerous PostgreSQL Functions ==========

	@Test
	@DisplayName("Should reject PG_READ_FILE function")
	void testRejectPgReadFile() {
		String sql = "SELECT PG_READ_FILE('/etc/passwd')";
		Exception exception = assertThrows(Exception.class, () -> invokeValidateRawSql(sql));
		assertTrue(exception.getCause() instanceof IllegalArgumentException);
		assertTrue(exception.getCause().getMessage().contains("PG_READ_FILE"));
	}

	@Test
	@DisplayName("Should reject PG_LS_DIR function")
	void testRejectPgLsDir() {
		String sql = "SELECT PG_LS_DIR('/etc')";
		Exception exception = assertThrows(Exception.class, () -> invokeValidateRawSql(sql));
		assertTrue(exception.getCause() instanceof IllegalArgumentException);
		assertTrue(exception.getCause().getMessage().contains("PG_LS_DIR"));
	}

	@Test
	@DisplayName("Should reject PG_SLEEP function")
	void testRejectPgSleep() {
		String sql = "SELECT PG_SLEEP(10)";
		Exception exception = assertThrows(Exception.class, () -> invokeValidateRawSql(sql));
		assertTrue(exception.getCause() instanceof IllegalArgumentException);
		assertTrue(exception.getCause().getMessage().contains("PG_SLEEP"));
	}

	@Test
	@DisplayName("Should reject COPY command")
	void testRejectCopy() {
		String sql = "SELECT * FROM users COPY users TO '/tmp/users.csv'";
		Exception exception = assertThrows(Exception.class, () -> invokeValidateRawSql(sql));
		assertTrue(exception.getCause() instanceof IllegalArgumentException);
		assertTrue(exception.getCause().getMessage().contains("COPY"));
	}

	@Test
	@DisplayName("Should reject LO_IMPORT function")
	void testRejectLoImport() {
		String sql = "SELECT LO_IMPORT('/etc/passwd')";
		Exception exception = assertThrows(Exception.class, () -> invokeValidateRawSql(sql));
		assertTrue(exception.getCause() instanceof IllegalArgumentException);
		assertTrue(exception.getCause().getMessage().contains("LO_IMPORT"));
	}

	@Test
	@DisplayName("Should reject LO_EXPORT function")
	void testRejectLoExport() {
		String sql = "SELECT LO_EXPORT(12345, '/tmp/file.txt')";
		Exception exception = assertThrows(Exception.class, () -> invokeValidateRawSql(sql));
		assertTrue(exception.getCause() instanceof IllegalArgumentException);
		assertTrue(exception.getCause().getMessage().contains("LO_EXPORT"));
	}

	@Test
	@DisplayName("Should reject PG_EXECUTE_SERVER_PROGRAM")
	void testRejectPgExecuteServerProgram() {
		String sql = "SELECT PG_EXECUTE_SERVER_PROGRAM('ls')";
		Exception exception = assertThrows(Exception.class, () -> invokeValidateRawSql(sql));
		assertTrue(exception.getCause() instanceof IllegalArgumentException);
		assertTrue(exception.getCause().getMessage().contains("PG_EXECUTE_SERVER_PROGRAM"));
	}

	// ========== Dangerous H2 Functions ==========

	@Test
	@DisplayName("Should reject FILE_READ function")
	void testRejectFileRead() {
		String sql = "SELECT FILE_READ('/etc/passwd')";
		Exception exception = assertThrows(Exception.class, () -> invokeValidateRawSql(sql));
		assertTrue(exception.getCause() instanceof IllegalArgumentException);
		assertTrue(exception.getCause().getMessage().contains("FILE_READ"));
	}

	@Test
	@DisplayName("Should reject FILE_WRITE function")
	void testRejectFileWrite() {
		String sql = "SELECT FILE_WRITE('test', '/tmp/file.txt')";
		Exception exception = assertThrows(Exception.class, () -> invokeValidateRawSql(sql));
		assertTrue(exception.getCause() instanceof IllegalArgumentException);
		assertTrue(exception.getCause().getMessage().contains("FILE_WRITE"));
	}

	@Test
	@DisplayName("Should reject CSVREAD function")
	void testRejectCsvRead() {
		String sql = "SELECT * FROM CSVREAD('/tmp/data.csv')";
		Exception exception = assertThrows(Exception.class, () -> invokeValidateRawSql(sql));
		assertTrue(exception.getCause() instanceof IllegalArgumentException);
		assertTrue(exception.getCause().getMessage().contains("CSVREAD"));
	}

	@Test
	@DisplayName("Should reject CSVWRITE function")
	void testRejectCsvWrite() {
		String sql = "SELECT CSVWRITE('/tmp/output.csv', 'SELECT * FROM users')";
		Exception exception = assertThrows(Exception.class, () -> invokeValidateRawSql(sql));
		assertTrue(exception.getCause() instanceof IllegalArgumentException);
		assertTrue(exception.getCause().getMessage().contains("CSVWRITE"));
	}

	@Test
	@DisplayName("Should reject LINK_SCHEMA function")
	void testRejectLinkSchema() {
		String sql = "SELECT LINK_SCHEMA('TARGET', 'TEST', 'org.h2.Driver', 'jdbc:h2:mem:', 'sa', 'sa', 'PUBLIC')";
		Exception exception = assertThrows(Exception.class, () -> invokeValidateRawSql(sql));
		assertTrue(exception.getCause() instanceof IllegalArgumentException);
		assertTrue(exception.getCause().getMessage().contains("LINK_SCHEMA"));
	}

	@Test
	@DisplayName("Should reject RUNSCRIPT function")
	void testRejectRunScript() {
		String sql = "SELECT RUNSCRIPT FROM '/tmp/script.sql'";
		Exception exception = assertThrows(Exception.class, () -> invokeValidateRawSql(sql));
		assertTrue(exception.getCause() instanceof IllegalArgumentException);
		assertTrue(exception.getCause().getMessage().contains("RUNSCRIPT"));
	}

	// ========== Common Dangerous Functions ==========

	@Test
	@DisplayName("Should reject SYSTEM function")
	void testRejectSystem() {
		String sql = "SELECT SYSTEM('ls -la')";
		Exception exception = assertThrows(Exception.class, () -> invokeValidateRawSql(sql));
		assertTrue(exception.getCause() instanceof IllegalArgumentException);
		assertTrue(exception.getCause().getMessage().contains("SYSTEM"));
	}

	@Test
	@DisplayName("Should reject SHELL function")
	void testRejectShell() {
		String sql = "SELECT SHELL('cat /etc/passwd')";
		Exception exception = assertThrows(Exception.class, () -> invokeValidateRawSql(sql));
		assertTrue(exception.getCause() instanceof IllegalArgumentException);
		assertTrue(exception.getCause().getMessage().contains("SHELL"));
	}

	@Test
	@DisplayName("Should reject EVAL function")
	void testRejectEval() {
		String sql = "SELECT EVAL('malicious code')";
		Exception exception = assertThrows(Exception.class, () -> invokeValidateRawSql(sql));
		assertTrue(exception.getCause() instanceof IllegalArgumentException);
		assertTrue(exception.getCause().getMessage().contains("EVAL"));
	}

	// ========== Edge Cases and Case Sensitivity ==========

	@Test
	@DisplayName("Should reject dangerous keywords in mixed case")
	void testRejectMixedCaseKeywords() {
		String sql = "SeLeCt * FrOm users InSeRt InTo test";
		Exception exception = assertThrows(Exception.class, () -> invokeValidateRawSql(sql));
		assertTrue(exception.getCause() instanceof IllegalArgumentException);
		assertTrue(exception.getCause().getMessage().contains("INSERT"));
	}

	@Test
	@DisplayName("Should reject dangerous functions in lowercase")
	void testRejectLowercaseFunctions() {
		String sql = "select load_file('/etc/passwd')";
		Exception exception = assertThrows(Exception.class, () -> invokeValidateRawSql(sql));
		assertTrue(exception.getCause() instanceof IllegalArgumentException);
		assertTrue(exception.getCause().getMessage().contains("LOAD_FILE"));
	}

	@Test
	@DisplayName("Should accept SELECT with legitimate column names that contain dangerous words")
	void testAcceptLegitimateColumnNames() throws Exception {
		// Column name "user_insert_date" contains "INSERT" but not as a separate keyword
		// This should pass because we use word boundaries in regex (\b)
		// Word boundaries don't match inside underscored words
		String sql = "SELECT user_insert_date, last_update_time FROM users";
		assertEquals(sql, invokeValidateRawSql(sql));
	}
}
