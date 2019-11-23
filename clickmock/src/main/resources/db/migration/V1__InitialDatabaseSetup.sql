-- Foreign table links.

CREATE LINKED TABLE __PARTICIPANT('com.microsoft.sqlserver.jdbc.SQLServerDriver', 'jdbc:sqlserver://localhost:1433;DatabaseName=test', 'admin', '12345678', '__Participant');
CREATE LINKED TABLE __PARTICIPANT_CUSTOMATTRIBUTESMANAGER('com.microsoft.sqlserver.jdbc.SQLServerDriver', 'jdbc:sqlserver://localhost:1433;DatabaseName=test', 'admin', '12345678', '__Participant_CustomAttributesManager');

