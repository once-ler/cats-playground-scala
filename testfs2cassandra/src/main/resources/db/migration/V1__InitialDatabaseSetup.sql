create table if not exists irb.document_metadata(
  id varchar(255), -- domain:root_type:root_id:doc_id (populate later for solr)
  domain varchar(25) not null,
  root_type varchar(255) not null,
  root_id varchar(120) not null,
  root_owner varchar(120),
  root_associates text[],
  root_company varchar(255),
  root_status varchar(255),
  root_display text,
  root_display_long text,
  doc_id varchar(120) not null,
  doc_other_id varchar(120),
  doc_file_path text,
  doc_object_path text,
  doc_category varchar(120),
  doc_name varchar(255),
  doc_date_created varchar(50),
  doc_year_created int,
  constraint irb_document_metadata_cluster_idx unique (domain, root_type, root_id, doc_id)
)
WITH (
  OIDS=FALSE
);

create table if not exists irb.document(
  doc_id varchar(120) not null,
  doc_other_id varchar(120),
  doc_xml xml
)
WITH (
  OIDS=FALSE
);

CREATE INDEX IF NOT EXISTS irb_document_doc_id_idx
  ON irb.document
  USING btree
  (doc_id);

