--
-- PostgreSQL database dump
--

\restrict SFfJnW81F1MtWqokrumIAJb1lSQqM8R2deAidMRVzCwYinzeCtV3RQf2sPd31CR

-- Dumped from database version 18.6 (Ubuntu 18.6-1.pgdg24.04+2)
-- Dumped by pg_dump version 18.6 (Ubuntu 18.6-1.pgdg24.04+2)

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- Data for Name: users; Type: TABLE DATA; Schema: public; Owner: postgres
--

SET SESSION AUTHORIZATION DEFAULT;

ALTER TABLE public.users DISABLE TRIGGER ALL;

COPY public.users (email_verifie, solde_fcfa, created_at, id, email, google_id, nom, password, prenom, role, statut, telephone, ville_cameroun) FROM stdin;
t	0.00	2026-07-22 10:09:32.141546	6	emmamataga18@gmail.com	\N	Gmail API	$2a$10$BKYg06RRBbsQ.ZPXFjeQM.V6dTg577uXBXRM6TJDxUuteYZ862Bq6	Test	USER	ACTIF	237600000001	\N
f	0.00	2026-07-23 08:53:01.354556	7	emmamataga@gmail.com	\N	Gmail API 2	$2a$10$Uzz.1QfIhi2TTQtIxePWDesb8d2B4bQ7ajfANq8TbmI/aPb8HPUhu	Test	USER	ACTIF	237600400002	\N
t	0.00	2026-07-30 09:54:10.938293	8	blancleroytatsinda@gmail.com	\N	blanc	$2a$10$UalI8k4Iv99mrDXXf6n1Cu80cTVkYBJNSNyJIPoE2VYghKHES5Ava	blanc	USER	ACTIF	+237696403420	\N
t	0.00	2026-08-04 12:39:05.130077	9	bertrandjuniortiotsedoungmene@gmail.com	\N	jun	$2a$10$wZZ9rYtfCestxQ7bfpkCFuGlBFvGktAOOgIyHJWZlIDapEpPpaTKO	jun	USER	ACTIF	237659010299	\N
t	0.00	2026-08-25 09:45:31.275749	10	test.b.fake@exemple.com	\N	User B	$2a$10$JdKRTJgMWM1lKULduTIX/O0c1peAS8rQN1zo6DkEJMbEHcEu8wPIi	Test	USER	ACTIF	237600030001	\N
t	7000.00	2026-07-19 14:25:01.213255	1	nzimbaaliocha15@gmail.com	\N	Test	$2a$10$6054Z1uPNW3/BiWdpmVdsOKREbgXlMRvIYEKuAxpf/5FxIUZGP/aC	Marie	ADMIN	ACTIF	237677889900	\N
\.


ALTER TABLE public.users ENABLE TRIGGER ALL;

--
-- Data for Name: account; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.account DISABLE TRIGGER ALL;

COPY public.account (balance, id, user_id) FROM stdin;
\.


ALTER TABLE public.account ENABLE TRIGGER ALL;

--
-- Data for Name: categories; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.categories DISABLE TRIGGER ALL;

COPY public.categories (active, id, description, nom, slug, icone_url) FROM stdin;
t	2f81c665-c4fe-462b-ade7-9e8644bef5eb	Marchés sportifs camerounais	Sport	sport	https://cdn.vision.cm/icons/sport.png
t	d16628e5-de39-4e74-88bc-9b12978ba77f	Marchés liés aux élections et décisions politiques	Politique	politique	https://cdn.vision.cm/icons/politique.png
\.


ALTER TABLE public.categories ENABLE TRIGGER ALL;

--
-- Data for Name: evenements; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.evenements DISABLE TRIGGER ALL;

COPY public.evenements (nb_traders, created_at, createur_id, date_fin, date_resolution, categorie_id, id, contexte, regles_resolution, source_resolution, statut, titre, icone_url, type_choix) FROM stdin;
0	2026-07-20 06:55:28.036285	1	2026-12-31 23:59:00	\N	2f81c665-c4fe-462b-ade7-9e8644bef5eb	2255575e-719b-47d7-9a4e-291c7c8c70b1	Éliminatoires en cours	Résolu OUI si qualifié officiellement	CAF	OUVERT	Le Cameroun se qualifie-t-il pour la CAN 2026 ?	https://cdn.vision.cm/icons/cameroun.png	UNIQUE
0	2026-08-21 10:13:19.898747	1	2026-12-31 23:59:00	\N	2f81c665-c4fe-462b-ade7-9e8644bef5eb	85f9bb84-c9f4-4294-8dd7-d0b512daa8d3	Éliminatoires en cours	Résolu OUI si qualifié officiellement	CAF	OUVERT	Le Cameroun se qualifie-t-il pour la CAN 2026 ?	https://cdn.vision.cm/icons/cameroun.png	UNIQUE
\.


ALTER TABLE public.evenements ENABLE TRIGGER ALL;

--
-- Data for Name: commentaires; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.commentaires DISABLE TRIGGER ALL;

COPY public.commentaires (nb_likes, signale, created_at, user_id, evenement_id, id, contenu) FROM stdin;
0	f	2026-08-25 15:59:22.857742	1	85f9bb84-c9f4-4294-8dd7-d0b512daa8d3	c0cae0b0-a020-4daa-9f64-079568dbfcd4	ne troquer pas vos principes pour l'argent
\.


ALTER TABLE public.commentaires ENABLE TRIGGER ALL;

--
-- Data for Name: likes; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.likes DISABLE TRIGGER ALL;

COPY public.likes (created_at, user_id, commentaire_id, id) FROM stdin;
\.


ALTER TABLE public.likes ENABLE TRIGGER ALL;

--
-- Data for Name: login_attempts; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.login_attempts DISABLE TRIGGER ALL;

COPY public.login_attempts (nb_echecs, bloque_jusqua, dernier_echec, id, identifier, ip_address) FROM stdin;
0	\N	\N	1	237677889900	\N
1	\N	2026-08-20 16:26:55.089714	2	nzimbaaliocha15gmail.com	\N
1	\N	2026-08-25 09:47:44.744231	3	test.b@exemple.com	\N
\.


ALTER TABLE public.login_attempts ENABLE TRIGGER ALL;

--
-- Data for Name: marches; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.marches DISABLE TRIGGER ALL;

COPY public.marches (resolved_at, evenement_id, id, outcome_gagnant, question, statut, icone_url, pool_non_fcfa, pool_oui_fcfa, description) FROM stdin;
2026-08-25 10:21:01.017162	85f9bb84-c9f4-4294-8dd7-d0b512daa8d3	63067d91-69a8-4ad8-8bf1-fa085e1c05b0	OUI	Le Cameroun est-il qualifié ?	RESOLU	\N	500.00	3000.00	\N
\N	2255575e-719b-47d7-9a4e-291c7c8c70b1	00662ca0-e077-4005-9d34-55338695736a	\N	Le Cameroun est-il qualifié ?	OUVERT	\N	0.00	3100.00	\N
\.


ALTER TABLE public.marches ENABLE TRIGGER ALL;

--
-- Data for Name: market_categories; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.market_categories DISABLE TRIGGER ALL;

COPY public.market_categories  FROM stdin;
\.


ALTER TABLE public.market_categories ENABLE TRIGGER ALL;

--
-- Data for Name: markets; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.markets DISABLE TRIGGER ALL;

COPY public.markets (id, outcome_gagnant, prix_non, prix_oui, question, resolved_at, statut, type_marche, volume_fcfa, evenement_id) FROM stdin;
\.


ALTER TABLE public.markets ENABLE TRIGGER ALL;

--
-- Data for Name: notifications; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.notifications DISABLE TRIGGER ALL;

COPY public.notifications (lue, created_at, user_id, id, message, titre, type_notif) FROM stdin;
f	2026-08-25 09:37:40.136179	1	61d4874e-0c40-4870-807b-53f933434171	Ceci est un test pour l’utilisateur A	Test notification A	GAIN
f	2026-08-25 10:21:01.024422	1	cdc06509-da7c-427d-bfa5-04e1c4eb4e50	Vous avez gagné 1166.67 FCFA sur le marché "Le Cameroun est-il qualifié ?".	Félicitations ! Vous avez gagné	GAIN
f	2026-08-25 10:21:01.050289	1	1217fbed-c40a-4650-b696-9fccc870c40f	Vous avez gagné 2333.33 FCFA sur le marché "Le Cameroun est-il qualifié ?".	Félicitations ! Vous avez gagné	GAIN
f	2026-08-25 10:21:01.050637	10	293d9bc5-7cfa-41e9-b1ad-a3f16c704f4b	Votre position sur "Le Cameroun est-il qualifié ?" a perdu.	Résultat du marché	PERTE
\.


ALTER TABLE public.notifications ENABLE TRIGGER ALL;

--
-- Data for Name: payment_providers; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.payment_providers DISABLE TRIGGER ALL;

COPY public.payment_providers (id, actif, code, mode, nom) FROM stdin;
047a1c4a-f12c-442c-8e82-c282a2f7175e	t	CINETPAY	SANDBOX	CinetPay
fb7603b4-3996-4fd3-a900-e1882aa8d4a7	t	NOKASH	SANDBOX	Nokash
\.


ALTER TABLE public.payment_providers ENABLE TRIGGER ALL;

--
-- Data for Name: positions; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.positions DISABLE TRIGGER ALL;

COPY public.positions (montant_mise_fcfa, created_at, user_id, id, marche_id, choix, statut) FROM stdin;
3000.00	2026-07-20 07:00:37.381314	1	bdbe531a-2487-4e76-9b70-ed3c68ab9b42	00662ca0-e077-4005-9d34-55338695736a	OUI	ACTIVE
1000.00	2026-08-21 10:58:18.162887	1	990b9c18-51dd-4fe9-9f7e-028fa3ead19b	63067d91-69a8-4ad8-8bf1-fa085e1c05b0	OUI	GAGNEE
2000.00	2026-08-21 11:00:37.434116	1	bc2e75ca-c5ac-4b44-bd13-383047c4146e	63067d91-69a8-4ad8-8bf1-fa085e1c05b0	OUI	GAGNEE
500.00	2026-08-25 10:06:38.130625	10	ef15e524-d640-495f-9ffe-5a5fd51ceb10	63067d91-69a8-4ad8-8bf1-fa085e1c05b0	NON	PERDUE
100.00	2026-08-26 10:55:04.541039	1	f00374b6-9511-4c16-848e-66ce6c131b2a	00662ca0-e077-4005-9d34-55338695736a	OUI	ACTIVE
\.


ALTER TABLE public.positions ENABLE TRIGGER ALL;

--
-- Data for Name: wallets; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.wallets DISABLE TRIGGER ALL;

COPY public.wallets (id, created_at, solde_fcfa, statut, updated_at, user_id) FROM stdin;
699b5571-1ef0-483e-99d7-027ba6b9945f	2026-08-25 10:02:37.413407	1500.00	ACTIF	2026-08-25 10:06:38.151372	10
a4f84f71-0b02-4b14-8d7f-317ceb5ebfc9	2026-08-07 16:27:34.669282	17400.00	ACTIF	2026-08-26 10:55:04.673109	1
\.


ALTER TABLE public.wallets ENABLE TRIGGER ALL;

--
-- Data for Name: wallet_transactions; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.wallet_transactions DISABLE TRIGGER ALL;

COPY public.wallet_transactions (id, categorie, created_at, description, montant_fcfa, reference_externe, solde_apres, solde_avant, statut, type, position_id, wallet_id) FROM stdin;
b0b8f4be-6825-4859-8525-637d70e287f8	DEPOT	2026-08-07 16:28:18.64327	Test dépôt wallet	10000.00	\N	10000.00	0.00	CONFIRME	CREDIT	\N	a4f84f71-0b02-4b14-8d7f-317ceb5ebfc9
e9038017-687a-4b00-8f57-efaaacc17eec	RETRAIT	2026-08-07 16:31:46.773838	Retrait vers +237690123456	1000.00	\N	10000.00	10000.00	EN_ATTENTE	DEBIT	\N	a4f84f71-0b02-4b14-8d7f-317ceb5ebfc9
a49d7f4a-2199-4fbe-a0a3-f1584a0babc0	DEPOT	2026-08-14 11:12:18.339393	Test dépôt Nokash sandbox	1000.00	\N	10000.00	10000.00	EN_ATTENTE	CREDIT	\N	a4f84f71-0b02-4b14-8d7f-317ceb5ebfc9
60a230e2-b1ac-4391-aceb-e45a6b5625b8	DEPOT	2026-08-14 11:24:02.592752	Test dépôt Nokash sandbox	1000.00	\N	10000.00	10000.00	EN_ATTENTE	CREDIT	\N	a4f84f71-0b02-4b14-8d7f-317ceb5ebfc9
a0712141-630a-4b60-b67a-f1c286545b4b	DEPOT	2026-08-14 11:33:05.498725	Test dépôt Nokash sandbox	1000.00	\N	10000.00	10000.00	EN_ATTENTE	CREDIT	\N	a4f84f71-0b02-4b14-8d7f-317ceb5ebfc9
aa9cd173-1670-4a8a-931d-06cfff0ac3b9	DEPOT	2026-08-14 11:48:20.667713	Test dépôt Nokash sandbox	1000.00	\N	10000.00	10000.00	ECHOUE	CREDIT	\N	a4f84f71-0b02-4b14-8d7f-317ceb5ebfc9
5892d13e-a689-4ce4-b64e-ececd5340e6c	DEPOT	2026-08-14 12:00:12.388867	Test succès Nokash sandbox	1000.00	\N	10000.00	10000.00	ECHOUE	CREDIT	\N	a4f84f71-0b02-4b14-8d7f-317ceb5ebfc9
dd787bad-9c41-453a-870d-be2dc1e01e62	DEPOT	2026-08-14 11:41:01.992459	Test dépôt Nokash sandbox	1000.00	\N	11000.00	10000.00	CONFIRME	CREDIT	\N	a4f84f71-0b02-4b14-8d7f-317ceb5ebfc9
f59ec0f4-6cfc-4587-9d82-916485e4c7b1	DEPOT	2026-08-17 14:22:52.177237	Rechargement de compte VISION	5000.00	\N	11000.00	11000.00	ECHOUE	CREDIT	\N	a4f84f71-0b02-4b14-8d7f-317ceb5ebfc9
726cdf01-073f-4167-bf3e-fbdb090011eb	DEPOT	2026-08-17 14:26:56.543973	Rechargement de compte VISION	5000.00	\N	11000.00	11000.00	ECHOUE	CREDIT	\N	a4f84f71-0b02-4b14-8d7f-317ceb5ebfc9
79842f8f-8e46-452a-8952-6d1ae3102645	DEPOT	2026-08-17 14:39:54.417236	Rechargement de compte VISION	5000.00	\N	11000.00	11000.00	ECHOUE	CREDIT	\N	a4f84f71-0b02-4b14-8d7f-317ceb5ebfc9
e5170f67-5faf-44b6-afe1-54852f652f7f	DEPOT	2026-08-18 09:40:39.085549	Rechargement de compte VISION	1000.00	\N	12000.00	11000.00	CONFIRME	CREDIT	\N	a4f84f71-0b02-4b14-8d7f-317ceb5ebfc9
07ebdf45-4f8c-4de1-b0dd-a6d7d4a834d1	DEPOT	2026-08-18 15:29:48.589821	Rechargement de compte VISION	1000.00	\N	12000.00	12000.00	ECHOUE	CREDIT	\N	a4f84f71-0b02-4b14-8d7f-317ceb5ebfc9
187d1fd8-58c7-4acb-80fc-ac271284c685	DEPOT	2026-08-20 16:28:34.030892	Rechargement de compte VISION	5000.00	\N	12000.00	12000.00	ECHOUE	CREDIT	\N	a4f84f71-0b02-4b14-8d7f-317ceb5ebfc9
8aebe963-c417-4656-afa3-7f29ee376a31	DEPOT	2026-08-21 10:01:13.836849	Rechargement de compte VISION	5000.00	\N	17000.00	12000.00	CONFIRME	CREDIT	\N	a4f84f71-0b02-4b14-8d7f-317ceb5ebfc9
950e2682-83e0-4b85-8fae-ae190da2312c	MISE	2026-08-21 10:58:18.19479	Mise sur "Le Cameroun est-il qualifié ?"	-1000.00	\N	16000.00	17000.00	CONFIRME	CREDIT	990b9c18-51dd-4fe9-9f7e-028fa3ead19b	a4f84f71-0b02-4b14-8d7f-317ceb5ebfc9
758aa776-2f2b-43dc-a74c-0da4fae30179	MISE	2026-08-21 11:00:37.472728	Mise sur "Le Cameroun est-il qualifié ?"	-2000.00	\N	14000.00	16000.00	CONFIRME	CREDIT	bc2e75ca-c5ac-4b44-bd13-383047c4146e	a4f84f71-0b02-4b14-8d7f-317ceb5ebfc9
060eeed2-fbb6-41ff-9466-7ba49d0f1f76	DEPOT	2026-08-25 10:03:18.989032	Test compte B	2000.00	\N	2000.00	0.00	CONFIRME	CREDIT	\N	699b5571-1ef0-483e-99d7-027ba6b9945f
ceeeb0f4-9021-42e9-9ef6-e5dd8b75ddb0	MISE	2026-08-25 10:06:38.144298	Mise sur "Le Cameroun est-il qualifié ?"	-500.00	\N	1500.00	2000.00	CONFIRME	CREDIT	ef15e524-d640-495f-9ffe-5a5fd51ceb10	699b5571-1ef0-483e-99d7-027ba6b9945f
32bfa012-32c4-4a26-81a3-cd8034bebea3	GAIN_RESOLUTION	2026-08-25 10:21:01.023527	Gain sur "Le Cameroun est-il qualifié ?"	1166.67	\N	15166.67	14000.00	CONFIRME	CREDIT	990b9c18-51dd-4fe9-9f7e-028fa3ead19b	a4f84f71-0b02-4b14-8d7f-317ceb5ebfc9
c61daaaa-1a9c-4b92-a583-f420c34e646d	GAIN_RESOLUTION	2026-08-25 10:21:01.049426	Gain sur "Le Cameroun est-il qualifié ?"	2333.33	\N	17500.00	15166.67	CONFIRME	CREDIT	bc2e75ca-c5ac-4b44-bd13-383047c4146e	a4f84f71-0b02-4b14-8d7f-317ceb5ebfc9
f04d6d87-7588-44e2-9257-07fb6089d85e	MISE	2026-08-26 10:55:04.662164	Mise sur "Le Cameroun est-il qualifié ?"	-100.00	\N	17400.00	17500.00	CONFIRME	CREDIT	f00374b6-9511-4c16-848e-66ce6c131b2a	a4f84f71-0b02-4b14-8d7f-317ceb5ebfc9
\.


ALTER TABLE public.wallet_transactions ENABLE TRIGGER ALL;

--
-- Data for Name: payment_intents; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.payment_intents DISABLE TRIGGER ALL;

COPY public.payment_intents (id, created_at, montant_fcfa, payload_reponse, provider_transaction_id, statut, updated_at, provider_id, wallet_transaction_id, order_id) FROM stdin;
ba657c43-56f7-4227-a3e6-809f9a29213a	2026-08-14 11:12:18.34219	1000.00	{"status":"REQUEST_OK","message":"paiement en cours de traitement","data":{"id":"66c530eaabf549588fcfc401753d14dd177640","status":"PENDING","amount":"1000.0","orderId":"ord_293cc41129a14a308b5e8a710b7a9481","phone":"237656638229","initiatedAt":"2026-08-14T11:12:19.303457","statusReason":null},"code":null}	66c530eaabf549588fcfc401753d14dd177640	EN_ATTENTE	2026-08-14 11:12:19.553602	fb7603b4-3996-4fd3-a900-e1882aa8d4a7	a49d7f4a-2199-4fbe-a0a3-f1584a0babc0	ord_293cc41129a14a308b5e8a710b7a9481
a04d81aa-6a2b-45ee-9adb-69e9af0d6ea7	2026-08-14 11:24:02.60528	1000.00	{"status":"REQUEST_OK","message":"paiement en cours de traitement","data":{"id":"d3214ac567684329a30a82c8c3824974177741","status":"PENDING","amount":"1000.0","orderId":"ord_42ba121d43774623a7b049d1e636127f","phone":"237656638229","initiatedAt":"2026-08-14T11:24:05.114890","statusReason":null},"code":null}	d3214ac567684329a30a82c8c3824974177741	EN_ATTENTE	2026-08-14 11:24:05.404177	fb7603b4-3996-4fd3-a900-e1882aa8d4a7	60a230e2-b1ac-4391-aceb-e45a6b5625b8	ord_42ba121d43774623a7b049d1e636127f
979140da-c300-4a68-bb58-646817db98b1	2026-08-14 11:33:05.515655	1000.00	{"status":"REQUEST_OK","message":"paiement en cours de traitement","data":{"id":"ba1e8a6069e7466f9de2a41a0ec94a96177810","status":"PENDING","amount":"1000.0","orderId":"ord_6a49e82e846941d7a5a1a3ec43fa934e","phone":"237656638229","initiatedAt":"2026-08-14T11:33:06.872999","statusReason":null},"code":null}	ba1e8a6069e7466f9de2a41a0ec94a96177810	EN_ATTENTE	2026-08-14 11:33:07.210968	fb7603b4-3996-4fd3-a900-e1882aa8d4a7	a0712141-630a-4b60-b67a-f1c286545b4b	ord_6a49e82e846941d7a5a1a3ec43fa934e
3e4505b2-fbe7-4d1d-858e-89e218c5ea82	2026-08-17 14:26:56.548956	5000.00	{"status":"REQUEST_OK","message":"paiement en cours de traitement","data":{"id":"3d4f758ef0814c65af7f8c8935d5a810203765","status":"PENDING","amount":"5000.0","orderId":"ord_eca651eec42c469aacca7ec9f2e700e8","phone":"237673943065","initiatedAt":"2026-08-17T14:26:57.925558","statusReason":null},"code":null}	3d4f758ef0814c65af7f8c8935d5a810203765	ECHOUE	2026-08-17 14:27:24.825913	fb7603b4-3996-4fd3-a900-e1882aa8d4a7	726cdf01-073f-4167-bf3e-fbdb090011eb	ord_eca651eec42c469aacca7ec9f2e700e8
c9bf4f94-f218-4ede-8186-d219c4d7ea43	2026-08-14 11:48:20.683923	1000.00	{"status":"REQUEST_OK","message":"paiement en cours de traitement","data":{"id":"e47d49e2d2634eb6a79dd25a55862d01177916","status":"PENDING","amount":"1000.0","orderId":"ord_ced1958e003c411c91c2ac54c5738119","phone":"237656638229","initiatedAt":"2026-08-14T11:48:23.879716","statusReason":null},"code":null}	e47d49e2d2634eb6a79dd25a55862d01177916	ECHOUE	2026-08-14 11:48:50.663877	fb7603b4-3996-4fd3-a900-e1882aa8d4a7	aa9cd173-1670-4a8a-931d-06cfff0ac3b9	ord_ced1958e003c411c91c2ac54c5738119
e14c6175-9bc2-44d6-b511-415fdfdc6ca7	2026-08-18 15:29:48.592272	1000.00	{"status":"REQUEST_OK","message":"paiement en cours de traitement","data":{"id":"1db09fd94b054d00a32e7d8d5ef5822f210847","status":"PENDING","amount":"1000.0","orderId":"ord_92618ccd3cfa4b63ae4977fa9c63f1ad","phone":"237673943065","initiatedAt":"2026-08-18T15:29:49.587498","statusReason":null},"code":null}	1db09fd94b054d00a32e7d8d5ef5822f210847	ECHOUE	2026-08-18 15:30:18.821513	fb7603b4-3996-4fd3-a900-e1882aa8d4a7	07ebdf45-4f8c-4de1-b0dd-a6d7d4a834d1	ord_92618ccd3cfa4b63ae4977fa9c63f1ad
9dc5bc49-4038-466b-9530-763e48eb38e9	2026-08-14 12:00:12.392222	1000.00	{"status":"REQUEST_OK","message":"paiement en cours de traitement","data":{"id":"34787e09dae14257b47ef2d770a46cac177998","status":"PENDING","amount":"1000.0","orderId":"ord_3079bcadc1eb49019c03eb5fa2449f39","phone":"237690000000","initiatedAt":"2026-08-14T12:00:14.283320","statusReason":null},"code":null}	34787e09dae14257b47ef2d770a46cac177998	ECHOUE	2026-08-14 12:00:43.401137	fb7603b4-3996-4fd3-a900-e1882aa8d4a7	5892d13e-a689-4ce4-b64e-ececd5340e6c	ord_3079bcadc1eb49019c03eb5fa2449f39
62f48dba-df37-43e6-b94f-ac50df41c1ef	2026-08-14 11:41:02.016638	1000.00	{"status":"REQUEST_OK","message":"paiement en cours de traitement","data":{"id":"6445d328c4bc4d2b8d23efe2f38ea3c5177864","status":"PENDING","amount":"1000.0","orderId":"ord_ffa145b802f44085be822de1df3e9d1b","phone":"237656638229","initiatedAt":"2026-08-14T11:41:03.717340","statusReason":null},"code":null}	6445d328c4bc4d2b8d23efe2f38ea3c5177864	REUSSI	2026-08-17 09:28:12.77379	fb7603b4-3996-4fd3-a900-e1882aa8d4a7	dd787bad-9c41-453a-870d-be2dc1e01e62	ord_ffa145b802f44085be822de1df3e9d1b
348c5096-573e-4ce1-a963-7097232fa2ac	2026-08-17 14:39:54.424581	5000.00	{"status":"REQUEST_OK","message":"paiement en cours de traitement","data":{"id":"fb0f56792bde48cd829454b0811f184f203855","status":"PENDING","amount":"5000.0","orderId":"ord_1e27828312464b50b6997523d77a58c8","phone":"237673943065","initiatedAt":"2026-08-17T14:39:55.039491","statusReason":null},"code":null}	fb0f56792bde48cd829454b0811f184f203855	ECHOUE	2026-08-17 14:40:23.244894	fb7603b4-3996-4fd3-a900-e1882aa8d4a7	79842f8f-8e46-452a-8952-6d1ae3102645	ord_1e27828312464b50b6997523d77a58c8
14b79d6d-55f8-41f7-9338-7b771191145c	2026-08-17 14:22:52.186516	5000.00	{"status":"REQUEST_OK","message":"paiement en cours de traitement","data":{"id":"36936a73a9064ae0973b61526a0df55e203732","status":"PENDING","amount":"5000.0","orderId":"ord_3ca058834f744ceab9d4908fc5184817","phone":"237656638229","initiatedAt":"2026-08-17T14:22:54.065919","statusReason":null},"code":null}	36936a73a9064ae0973b61526a0df55e203732	ECHOUE	2026-08-17 14:23:22.429963	fb7603b4-3996-4fd3-a900-e1882aa8d4a7	f59ec0f4-6cfc-4587-9d82-916485e4c7b1	ord_3ca058834f744ceab9d4908fc5184817
d085cc6a-3f90-468c-87c4-84f25f5215db	2026-08-18 09:40:39.097499	1000.00	{"status":"REQUEST_OK","message":"paiement en cours de traitement","data":{"id":"a79d3e328e8541fc8301f4cf29511474208645","status":"PENDING","amount":"1000.0","orderId":"ord_286f4d9eeb0f4e76a79bb088a273aa33","phone":"237673943065","initiatedAt":"2026-08-18T09:40:50.335929","statusReason":null},"code":null}	a79d3e328e8541fc8301f4cf29511474208645	REUSSI	2026-08-18 09:51:19.655721	fb7603b4-3996-4fd3-a900-e1882aa8d4a7	e5170f67-5faf-44b6-afe1-54852f652f7f	ord_286f4d9eeb0f4e76a79bb088a273aa33
13ec7c6e-6ae9-4b17-b4fc-d997bc66414f	2026-08-21 10:01:13.846333	5000.00	{"status":"REQUEST_OK","message":"paiement en cours de traitement","data":{"id":"0613875089f44e0aa138c758b561d006233748","status":"PENDING","amount":"5000.0","orderId":"ord_fc7023efe3474b2ba8268962f1c4169d","phone":"237673943065","initiatedAt":"2026-08-21T10:01:15.464995","statusReason":null},"code":null}	0613875089f44e0aa138c758b561d006233748	REUSSI	2026-08-21 10:07:27.552509	fb7603b4-3996-4fd3-a900-e1882aa8d4a7	8aebe963-c417-4656-afa3-7f29ee376a31	ord_fc7023efe3474b2ba8268962f1c4169d
17eaef39-481c-49bc-b3eb-1ae75d45e068	2026-08-20 16:28:34.042768	5000.00	{"status":"REQUEST_OK","message":"paiement en cours de traitement","data":{"id":"ef4d2fb7ca1e40f4b7515ecdced6354a227766","status":"PENDING","amount":"5000.0","orderId":"ord_368fe25bb9d64900979fe3524e47fcd5","phone":"237673943065","initiatedAt":"2026-08-20T16:28:37.691611","statusReason":null},"code":null}	ef4d2fb7ca1e40f4b7515ecdced6354a227766	ECHOUE	2026-08-20 16:34:38.751896	fb7603b4-3996-4fd3-a900-e1882aa8d4a7	187d1fd8-58c7-4acb-80fc-ac271284c685	ord_368fe25bb9d64900979fe3524e47fcd5
7f184ead-355f-465f-9ce4-66e62c9c01e1	2026-08-25 10:03:18.997196	2000.00	{"status":"REQUEST_OK","message":"paiement en cours de traitement","data":{"id":"95e05287f64c44359915ae81071c5c2423848","status":"PENDING","amount":"2000.0","orderId":"ord_2079e2bbe71a4d598a0446463d425adb","phone":"237690000000","initiatedAt":"2026-08-25T10:03:21.797660","statusReason":null},"code":null}	95e05287f64c44359915ae81071c5c2423848	REUSSI	2026-08-25 10:05:39.198185	fb7603b4-3996-4fd3-a900-e1882aa8d4a7	060eeed2-fbb6-41ff-9466-7ba49d0f1f76	ord_2079e2bbe71a4d598a0446463d425adb
\.


ALTER TABLE public.payment_intents ENABLE TRIGGER ALL;

--
-- Data for Name: reset_tokens; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.reset_tokens DISABLE TRIGGER ALL;

COPY public.reset_tokens (expiry_date, id, email, token) FROM stdin;
\.


ALTER TABLE public.reset_tokens ENABLE TRIGGER ALL;

--
-- Data for Name: token; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.token DISABLE TRIGGER ALL;

COPY public.token (expiry_date, id, email, token) FROM stdin;
\.


ALTER TABLE public.token ENABLE TRIGGER ALL;

--
-- Data for Name: transactions; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.transactions DISABLE TRIGGER ALL;

COPY public.transactions (montant_fcfa, created_at, user_id, id, position_id, operateur, reference_mobile_money, statut_paiement, type_tx) FROM stdin;
10000.00	2026-07-20 06:58:14.435337	1	e5488d7d-db83-4ab7-9445-6de4de0a03e3	\N	ORANGE_MONEY	TEST-DEPOT-001	CONFIRME	DEPOT
3000.00	2026-07-20 07:00:37.38302	1	0b88ebe4-ca3b-4deb-ae49-5383f67ae8bf	bdbe531a-2487-4e76-9b70-ed3c68ab9b42	\N	\N	CONFIRME	ACHAT_PARTS
1000.00	2026-08-21 10:58:18.195336	1	ae7571a3-c2db-440d-b0df-be78f8e95629	990b9c18-51dd-4fe9-9f7e-028fa3ead19b	\N	\N	CONFIRME	ACHAT_PARTS
2000.00	2026-08-21 11:00:37.473166	1	c6977c19-8d6b-4f19-b48f-2db0ab99d9a4	bc2e75ca-c5ac-4b44-bd13-383047c4146e	\N	\N	CONFIRME	ACHAT_PARTS
500.00	2026-08-25 10:06:38.148408	10	7facb52f-5013-4a79-b986-37aa23bc2123	ef15e524-d640-495f-9ffe-5a5fd51ceb10	\N	\N	CONFIRME	ACHAT_PARTS
1166.67	2026-08-25 10:21:01.023832	1	913da9e8-c30c-4fe5-88c5-cd66840f2b8a	990b9c18-51dd-4fe9-9f7e-028fa3ead19b	\N	\N	CONFIRME	GAIN_RESOLUTION
2333.33	2026-08-25 10:21:01.049824	1	d2be861a-96c8-4466-be0b-1b076aa79fb1	bc2e75ca-c5ac-4b44-bd13-383047c4146e	\N	\N	CONFIRME	GAIN_RESOLUTION
100.00	2026-08-26 10:55:04.663787	1	6e760bdb-5bee-40b3-85f0-62e5aeda8aab	f00374b6-9511-4c16-848e-66ce6c131b2a	\N	\N	CONFIRME	ACHAT_PARTS
\.


ALTER TABLE public.transactions ENABLE TRIGGER ALL;

--
-- Data for Name: webhook_logs; Type: TABLE DATA; Schema: public; Owner: postgres
--

ALTER TABLE public.webhook_logs DISABLE TRIGGER ALL;

COPY public.webhook_logs (id, created_at, payload_brut, signature_valide, traite, provider_id) FROM stdin;
27fb5e55-e4c7-498a-aea6-83897a90f5aa	2026-08-14 11:33:33.009764	{"amount":"1000","initiatedAt":[2026,8,14,11,33,6,872000000],"statusReason":"BALANCE_INSUFFICIENT","orderId":"ord_6a49e82e846941d7a5a1a3ec43fa934e","phone":"237656638229","id":"ba1e8a6069e7466f9de2a41a0ec94a96177810","status":"FAILED"}	t	f	fb7603b4-3996-4fd3-a900-e1882aa8d4a7
e550c82b-4880-4bfe-87ff-99cc7b8868f3	2026-08-14 11:41:30.088246	{"amount":"1000","initiatedAt":[2026,8,14,11,41,3,717000000],"statusReason":"BALANCE_INSUFFICIENT","orderId":"ord_ffa145b802f44085be822de1df3e9d1b","phone":"237656638229","id":"6445d328c4bc4d2b8d23efe2f38ea3c5177864","status":"FAILED"}	t	f	fb7603b4-3996-4fd3-a900-e1882aa8d4a7
18b1a6ed-0654-4d08-a00e-27a3dad97493	2026-08-14 11:48:50.662696	{"amount":"1000","initiatedAt":[2026,8,14,11,48,23,879000000],"statusReason":"BALANCE_INSUFFICIENT","orderId":"ord_ced1958e003c411c91c2ac54c5738119","phone":"237656638229","id":"e47d49e2d2634eb6a79dd25a55862d01177916","status":"FAILED"}	t	t	fb7603b4-3996-4fd3-a900-e1882aa8d4a7
0fa852bd-65a0-4e34-b839-b059104b25ce	2026-08-14 12:00:43.39966	{"amount":"1000","initiatedAt":[2026,8,14,12,0,14,283000000],"statusReason":"99051 ::  Beneficiaire introuvable","orderId":"ord_3079bcadc1eb49019c03eb5fa2449f39","phone":"237690000000","id":"34787e09dae14257b47ef2d770a46cac177998","status":"FAILED"}	t	t	fb7603b4-3996-4fd3-a900-e1882aa8d4a7
b3bb08ac-9c33-42ac-a78e-527a3634859e	2026-08-14 12:24:10.176997	"string"	t	f	fb7603b4-3996-4fd3-a900-e1882aa8d4a7
6ee83372-dfa0-47aa-8a90-4729f08e374e	2026-08-17 09:28:12.767193	{"id":"test_manuel_001","status":"SUCCESS","amount":1000,"phone":"237656638229","orderId":"ord_ffa145b802f44085be822de1df3e9d1b","statusReason":null}	t	t	fb7603b4-3996-4fd3-a900-e1882aa8d4a7
69a27864-42ac-4e28-ab41-7e092d4f24b4	2026-08-17 14:23:22.428529	{"id":"36936a73a9064ae0973b61526a0df55e203732","status":"FAILED","amount":5000,"phone":"237656638229","orderId":"ord_3ca058834f744ceab9d4908fc5184817","statusReason":"BALANCE_INSUFFICIENT"}	t	t	fb7603b4-3996-4fd3-a900-e1882aa8d4a7
3b044b55-661c-432e-b57e-8118508e8619	2026-08-17 14:27:24.823298	{"id":"3d4f758ef0814c65af7f8c8935d5a810203765","status":"FAILED","amount":5000,"phone":"237673943065","orderId":"ord_eca651eec42c469aacca7ec9f2e700e8","statusReason":"LOW_BALANCE_OR_PAYEE_LIMIT_REACHED_OR_NOT_ALLOWED"}	t	t	fb7603b4-3996-4fd3-a900-e1882aa8d4a7
7be046be-c1ec-42d8-875c-469879c80ff1	2026-08-17 14:40:23.243954	{"id":"fb0f56792bde48cd829454b0811f184f203855","status":"FAILED","amount":5000,"phone":"237673943065","orderId":"ord_1e27828312464b50b6997523d77a58c8","statusReason":"LOW_BALANCE_OR_PAYEE_LIMIT_REACHED_OR_NOT_ALLOWED"}	t	t	fb7603b4-3996-4fd3-a900-e1882aa8d4a7
c9ab9aef-b84f-416e-b395-34899623c885	2026-08-17 14:40:24.249232	{"id":"test_manuel_001","status":"SUCCESS","amount":1000,"phone":"237673943065","orderId":"ord_726cdf01-073f-4167-bf3e-fbdb090011eb","statusReason":null}	t	f	fb7603b4-3996-4fd3-a900-e1882aa8d4a7
ee81a95e-4e15-48dd-9f52-24d56c66096e	2026-08-17 14:41:46.669558	{"id":"test_manuel_001","status":"SUCCESS","amount":1000,"phone":"237673943065","orderId":"79842f8f-8e46-452a-8952-6d1ae3102645","statusReason":null}	t	f	fb7603b4-3996-4fd3-a900-e1882aa8d4a7
5ec60781-0430-4946-baca-9b1a0e9bdf35	2026-08-17 14:42:00.45883	{"id":"test_manuel_001","status":"SUCCESS","amount":1000,"phone":"237673943065","orderId":"ord_79842f8f-8e46-452a-8952-6d1ae3102645","statusReason":null}	t	f	fb7603b4-3996-4fd3-a900-e1882aa8d4a7
0d236449-9471-4273-8863-1d6fda2261f3	2026-08-18 09:51:19.655369	{"id":"test3","status":"SUCCESS","amount":1000,"phone":"673943065","orderId":"ord_286f4d9eeb0f4e76a79bb088a273aa33","statusReason":"optionnel"}	t	t	fb7603b4-3996-4fd3-a900-e1882aa8d4a7
d6e0ec2a-db8d-492c-8d6b-5d35ce0ed452	2026-08-18 15:30:18.820653	{"id":"1db09fd94b054d00a32e7d8d5ef5822f210847","status":"FAILED","amount":1000,"phone":"237673943065","orderId":"ord_92618ccd3cfa4b63ae4977fa9c63f1ad"}	t	t	fb7603b4-3996-4fd3-a900-e1882aa8d4a7
cc8accce-78b6-4227-8429-cb0c315dae07	2026-08-20 16:34:38.751293	{"id":"nouveau","status":"SUCCESS","amount":3000,"phone":"673943065","orderId":"ord_368fe25bb9d64900979fe3524e47fcd5"}	t	t	fb7603b4-3996-4fd3-a900-e1882aa8d4a7
451ae84e-8521-40fb-aad4-6cd350b581e8	2026-08-20 16:35:47.120835	{"id":"nouveau","status":"SUCCESS","amount":1000,"phone":"673943065","orderId":"ord_368fe25bb9d64900979fe3524e47fcd5"}	t	t	fb7603b4-3996-4fd3-a900-e1882aa8d4a7
c8f7623e-f07c-4083-a125-4d0bafed2dd3	2026-08-21 10:07:27.552119	{"id":"test5","status":"SUCCESS","amount":5000,"phone":"237673943065","orderId":"ord_fc7023efe3474b2ba8268962f1c4169d"}	t	t	fb7603b4-3996-4fd3-a900-e1882aa8d4a7
5a01491d-bf43-4fda-a1b0-3441e19e00b0	2026-08-25 10:05:39.197479	{"id":"test_b_depot","status":"SUCCESS","amount":2000,"phone":"237690000000","orderId":"ord_2079e2bbe71a4d598a0446463d425adb"}	t	t	fb7603b4-3996-4fd3-a900-e1882aa8d4a7
\.


ALTER TABLE public.webhook_logs ENABLE TRIGGER ALL;

--
-- Name: account_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.account_id_seq', 1, false);


--
-- Name: login_attempts_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.login_attempts_id_seq', 3, true);


--
-- Name: reset_tokens_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.reset_tokens_id_seq', 6, true);


--
-- Name: token_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.token_id_seq', 1, false);


--
-- Name: users_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.users_id_seq', 10, true);


--
-- PostgreSQL database dump complete
--

\unrestrict SFfJnW81F1MtWqokrumIAJb1lSQqM8R2deAidMRVzCwYinzeCtV3RQf2sPd31CR

