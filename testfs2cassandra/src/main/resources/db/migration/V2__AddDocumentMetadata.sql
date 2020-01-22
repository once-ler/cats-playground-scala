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
('foo', 'bar', 'F', 'doc9', 'A900'),
('foo', 'bar', 'A', 'doc10', 'A100'),
('foo', 'bar', 'B', 'doc11', 'A110'),
('foo', 'bar', 'C', 'doc12', 'A120'),
('foo', 'bar', 'C', 'doc13', 'A130'),
('foo', 'bar', 'D', 'doc14', 'A140'),
('foo', 'bar', 'D', 'doc15', 'A150'),
('foo', 'bar', 'D', 'doc16', 'A160'),
('foo', 'bar', 'E', 'doc17', 'A170'),
('foo', 'bar', 'E', 'doc18', 'A180'),
('foo', 'bar', 'F', 'doc19', 'A190'),
('foo', 'bar', 'A', 'doc20', 'A200'),
('foo', 'bar', 'B', 'doc21', 'A210'),
('foo', 'bar', 'C', 'doc22', 'A220'),
('foo', 'bar', 'C', 'doc23', 'A230'),
('foo', 'bar', 'D', 'doc24', 'A240'),
('foo', 'bar', 'D', 'doc25', 'A250'),
('foo', 'bar', 'D', 'doc26', 'A260'),
('foo', 'bar', 'E', 'doc27', 'A270'),
('foo', 'bar', 'E', 'doc28', 'A280'),
('foo', 'bar', 'F', 'doc29', 'A290')
on conflict on constraint irb_document_metadata_cluster_idx
do update
set doc_other_id = excluded.doc_other_id
;
*/