CREATE TABLE PATIENT (
  ADMINISTRATIVE_SEX VARCHAR,
  DATE_TIMEOF_BIRTH VARCHAR,
  ETHNIC_GROUP VARCHAR,
  PATIENT_ADDRESS VARCHAR,
  PATIENT_NAME VARCHAR,
  PHONE_NUMBER_HOME VARCHAR,
  RACE VARCHAR,
  MRN VARCHAR,
  DATE_CREATED BIGINT,
  DATE_LOCAL VARCHAR
);

CREATE LINKED TABLE __PARTICIPANT('com.microsoft.sqlserver.jdbc.SQLServerDriver', 'jdbc:sqlserver://localhost:1433;DatabaseName=test', 'admin', '12345678', '__Participant');
CREATE LINKED TABLE __PARTICIPANT_CUSTOMATTRIBUTESMANAGER('com.microsoft.sqlserver.jdbc.SQLServerDriver', 'jdbc:sqlserver://localhost:1433;DatabaseName=test', 'admin', '12345678', '__Participant_CustomAttributesManager');

CREATE LINKED TABLE PATIENT_REMOTE('com.microsoft.sqlserver.jdbc.SQLServerDriver', 'jdbc:sqlserver://localhost:1433;DatabaseName=test', 'admin', '12345678', 'epic.patient_demographic_hist');
CREATE LINKED TABLE PATIENT_ETHNICITY('com.microsoft.sqlserver.jdbc.SQLServerDriver', 'jdbc:sqlserver://localhost:1433;DatabaseName=test', 'admin', '12345678', 'epic.ethnicity');
CREATE LINKED TABLE PATIENT_RACE('com.microsoft.sqlserver.jdbc.SQLServerDriver', 'jdbc:sqlserver://localhost:1433;DatabaseName=test', 'admin', '12345678', 'epic.race');
CREATE LINKED TABLE PATIENT_STATE('com.microsoft.sqlserver.jdbc.SQLServerDriver', 'jdbc:sqlserver://localhost:1433;DatabaseName=test', 'admin', '12345678', 'epic.state');

