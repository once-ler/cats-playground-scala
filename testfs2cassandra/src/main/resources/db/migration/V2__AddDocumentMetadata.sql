/*
insert into irb.document_metadata ("domain", root_type, root_id, doc_id, doc_other_id)
values
('foo', 'bar', 'A', 'doc0', 'A000'),
('foo', 'bar', 'B', 'doc1', 'A100'),
('foo', 'bar', 'C', 'doc2', 'A200'),
('foo', 'bar', 'C', 'doc3', 'A300'),
('foo', 'bar', 'D', 'doc4', 'A400'),
('foo', 'bar', 'D', 'doc5', 'A500'),
('foo', 'bar', 'D', 'doc6', 'A600'),
('foo', 'bar', 'E', 'doc7', 'A700'),
('foo', 'bar', 'E', 'doc8', 'A800'),
('foo', 'bar', 'F', 'doc9', 'A900')
on conflict on constraint irb_document_metadata_cluster_idx
do update
set doc_other_id = excluded.doc_other_id
;
*/