INSERT into network_type (network_type_id, name, image_id) values (77, 'chatbot', 'e');
delete from network_type where network_type_id=77;

INSERT into network_type ( name, image_id) values ( 'image', 'a');
INSERT into network_type ( name, image_id) values ( 'image_classification', 'localhost:5000/image_classification');
INSERT into network_type ( name, image_id) values ( 'log', 'c');
INSERT into network_type ( name, image_id) values ('log_preitrained', 'd');
INSERT into network_type ( name, image_id) values ( 'chatbot', 'e');
INSERT into chobot_user (login, password, first_name, last_name, email, secret) values ('stejskys', 'heslo', 'Misa', 'stejskal', 'stejsky.s@mail.com', 'dGFqbmVIZXNsbw==');
-- insert into network (name, status, network_type_id, api_key) values('image-classificator', 0,1, 'dGFqbmVIZXNsbw==');


-- (0, 0, 'imagator', 'tag site', 1, 'localhost:5001', 'nejakej registr')

-- INSERT into user_application values (0, 'nejakej name', 'nejaka desc', 0, 0, 0);
-- insert into student values(10001,'Ranga', 'E1234567');