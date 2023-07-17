-- User 데이터 생성
-- 패스워드: tester2022
INSERT INTO users(seq, name, email, password, roles)
VALUES (null, 'tester', 'tester123@testing.com', '$2a$10$lou9YrWUrAxoea2wuQExRu1i1gkqvvOZTlZUPQ7fF6y50wBquFB8W',
        'ROLE_ADMIN');

-- Location 데이터 생성
INSERT INTO locations(seq, name, details, address, x_axis, y_axis, user_seq, create_at, image_path)
VALUES (null, '노량진역', '서울 지하철의 중심지','서울특별시 동작구 노량진로 지하130', 37.517, 126.942, 1, now(), '/mapImages/noryangin.jfif');
INSERT INTO locations(seq, name, details, address, x_axis, y_axis, user_seq, create_at, image_path)
VALUES (null, '한강대교', '한강에 있는 대교','대한민국 서울 용산구 이촌1동', 37.515435, 126.95687, 1, now(), '/mapImages/hangangDaegyo.jfif');
INSERT INTO locations(seq, name, details, address, x_axis, y_axis, user_seq, create_at, image_path)
VALUES (null, '국립 서울 현충원', '영웅들의 요람','대한민국 서울 동작구 사당2동', 37.498918, 126.971811, 1, now(), '/mapImages/nationalSeoulHyunChungWon.jfif');
INSERT INTO locations(seq, name, details, address, x_axis, y_axis, user_seq, create_at, image_path)
VALUES (null, '노들섬', '조깅하기 괜찮지만 커플들의 핫 플레이스','대한민국 서울 용산구 이촌1동', 37.517433, 126.958963, 1, now(), '/mapImages/nodeul_island.jpg');
INSERT INTO locations(seq, name, details, address, x_axis, y_axis, user_seq, create_at, image_path)
VALUES (null, 'IFC 서울', '여의도에서 쇼핑하기 좋은 곳','서울 영등포구 국제금융로 지하 15', 37.525262, 126.925582, 1, now(), null);
INSERT INTO locations(seq, name, details, address, x_axis, y_axis, user_seq, create_at, image_path)
VALUES (null, '춘천역', '춘천에 가본적은 없지만 춘천가는 기차는..','강원 춘천시 공지로 591', 37.8479693, 127.754541, 1, now(), null);
INSERT INTO locations(seq, name, details, address, x_axis, y_axis, user_seq, create_at, image_path)
VALUES (null, '요코하마', '요코하마','일본 요코하마시  Naka Ward 中庭棟', 35.4437078, 139.6380256, 1, now(), null);
INSERT INTO locations(seq, name, details, address, x_axis, y_axis, user_seq, create_at, image_path)
VALUES (null, '인천광역시청', '인천광역시청','인천 남동구 구월로 지하 99', 37.4550974, 126.7054768, 1, now(), null);
INSERT INTO locations(seq, name, details, address, x_axis, y_axis, user_seq, create_at, image_path)
VALUES (null, '광주광역시청', '광주광역시청','광주 서구 내방로 111', 35.160126000000005, 126.85176292036687, 1, now(), null);
INSERT INTO locations(seq, name, details, address, x_axis, y_axis, user_seq, create_at, image_path)
VALUES (null, '제주도청', '제주도청','제주특별자치도 제주시 문연로 6', 33.4897035, 126.5004032, 1, now(), null);