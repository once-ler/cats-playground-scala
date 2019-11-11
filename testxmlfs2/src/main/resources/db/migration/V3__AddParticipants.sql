insert into PARTICIPANT (oid, medicalRecordNumber) select 0xa14e95f30b8c23d7, '10038996' from PARTICIPANT
where not exists (select medicalRecordNumber from PARTICIPANT where medicalRecordNumber = '10038996');

insert into PARTICIPANT (oid, medicalRecordNumber) select 0xb517bc2de64cc314, '10038998' from PARTICIPANT
where not exists (select medicalRecordNumber from PARTICIPANT where medicalRecordNumber = '10038998');

insert into PARTICIPANT (oid, medicalRecordNumber) select 0x74b32ff8cc2aaf79, '52058523' from PARTICIPANT
where not exists (select medicalRecordNumber from PARTICIPANT where medicalRecordNumber = '52058523');

insert into PARTICIPANT (oid, medicalRecordNumber) select 0x12c308f7e45b3151, '52065991' from PARTICIPANT
where not exists (select medicalRecordNumber from PARTICIPANT where medicalRecordNumber = '52065991');

insert into PARTICIPANT (oid, medicalRecordNumber) select 0x5b23afbe802661d6, '10038181' from PARTICIPANT
where not exists (select medicalRecordNumber from PARTICIPANT where medicalRecordNumber = '10038181');
