INSERT into network_type (network_type_id, name, image_id) values (77, 'chatbot', 'e');
delete from network_type where network_type_id=77;


-- INSERT INTO public.network_type (network_type_id, image_id, name) VALUES (1, 'localhost:5000/chobot_images_custom', 'image_custom_classification');
-- INSERT INTO public.network_type (network_type_id, image_id, name) VALUES (4, 'localhost:5000/chobot_chatbot', 'chatbot');
-- INSERT INTO public.network_type (network_type_id, image_id, name) VALUES (2, 'localhost:5000/chobot_images_pretrained', 'image_classification');
-- INSERT INTO public.network_type (network_type_id, image_id, name) VALUES (3, 'localhost:5000/chobot_logger', 'log_classification');
--
-- INSERT INTO public.chobot_user (user_id, email, first_name, last_name, login, password, secret) VALUES (1, 'stejsky.s@mail.com', 'Misa', 'stejskal', 'stejskys', 'heslo', 'dGFqbmVIZXNsbw==');