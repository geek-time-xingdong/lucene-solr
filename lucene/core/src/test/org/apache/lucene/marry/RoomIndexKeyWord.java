package org.apache.lucene.marry;

/**
 * @author chengzhengzheng
 * @date 2021/1/12
 */
public interface RoomIndexKeyWord {

 String INDEX_TYPE = "index_type";
 String ROOM_TYPE = "1";
 String USER_TYPE = "0";
 //公共

 /**
  * 用户属性
  */
 String USER_FACE_SCORE  = "user_face_score";
 String USER_REAL_PERSON = "user_real_person";
 String USER_AGE = "user_age";
 String USER_SEX     = "user_sex";
 String USER_SEAT_ID  = "user_seat_id";
 String USER_LOCATION = "user_location";
 String USER_LAT    = "user_lot";
 String USER_LON    = "user_lon";
 String USER_IS_NEW = "user_is_new";
 String USER_PROV_CODE = "user_pro_code";
 String USER_AVATAR = "user_avatar";
 String USER_NAME              = "user_name";
 String USER_MOMOID                 = "user_momoid";
 String USER_BECOME_MATCHMAKER_TIME = "become_matchmaker_time";
 String USER_ONLINE_STATUS = "online_status";
 String USER_ROLE          = "user_role";
 String USER_CID           = "user_cid";

 /**
  * 房间属性
  */
 String ROOM_MODE        = "room_mode";
 String ROOM_OWNER       = "room_owner";
 String ROOM_SERVER_TYPE = "room_server_type";
 String ROOM_NOTICE      = "room_notice";
 String ROOM_STAGE       = "room_stage";
 String ROOM_WHITE_ROOM  = "room_white_room";
 String ROOM_CID         = "room_cid";
 String ROOM_MALE_GUEST_SIZE         = "room_male_guest_size";
 String ROOM_FEMALE_GUEST_SIZE         = "room_female_guest_size";


 /**
  * 推荐列表用户
  */
 String RECOMMEND_ID = "recommend_id";
 String RECOMMEND_FACE_SCORE = "recommend_face_score";
 String RECOMMEND_LAST_ONLINE_TIME = "last_app_online_time";
 String RECOMMEND_REAL_PERSON     = "recommend_real_person";
 String RECOMMEND_LEVEL           = "recommend_level";
 String RECOMMEND_LAST_MARRY_TIME = "last_marry_online_time";
 String RECOMMEND_AVATAR = "recommend_avatar";
 String RECOMMEND_AGE = "recommend_age";
 String RECOMMEND_LOC = "recommend_loc";

 String RECOMMEND_LON = "recommend_lon";
 String RECOMMEND_LAT = "recommend_lat";



}
