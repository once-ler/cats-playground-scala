-- Foreign table links.

CREATE LINKED TABLE __PARTICIPANT('com.microsoft.sqlserver.jdbc.SQLServerDriver', 'jdbc:sqlserver://localhost:1433;DatabaseName=test', 'admin', '12345678', '__Participant');
CREATE LINKED TABLE __PARTICIPANT_CUSTOMATTRIBUTESMANAGER('com.microsoft.sqlserver.jdbc.SQLServerDriver', 'jdbc:sqlserver://localhost:1433;DatabaseName=test', 'admin', '12345678', '__Participant_CustomAttributesManager');

CREATE LINKED TABLE _PARTY('com.microsoft.sqlserver.jdbc.SQLServerDriver', 'jdbc:sqlserver://localhost:1433;DatabaseName=test', 'admin', '12345678', '_Party');
CREATE LINKED TABLE _PARTYCONTACTINFORMATION('com.microsoft.sqlserver.jdbc.SQLServerDriver', 'jdbc:sqlserver://localhost:1433;DatabaseName=test', 'admin', '12345678', '_PartyContactInformation');
CREATE LINKED TABLE [_POSTAL CONTACT INFORMATION]('com.microsoft.sqlserver.jdbc.SQLServerDriver', 'jdbc:sqlserver://localhost:1433;DatabaseName=test', 'admin', '12345678', '[_Postal Contact Information]');
CREATE LINKED TABLE [_PHONE CONTACT INFORMATION]('com.microsoft.sqlserver.jdbc.SQLServerDriver', 'jdbc:sqlserver://localhost:1433;DatabaseName=test', 'admin', '12345678', '[_Phone Contact Information]');
CREATE LINKED TABLE [_E-MAIL CONTACT INFORMATION]('com.microsoft.sqlserver.jdbc.SQLServerDriver', 'jdbc:sqlserver://localhost:1433;DatabaseName=test', 'admin', '12345678', '[_E-mail Contact Information]');
