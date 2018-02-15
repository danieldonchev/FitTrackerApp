package com.daniel.FitTrackerApp;

public final class API {
    //base url address
//    private static final String baseURLHttps = "https://95.42.100.225:8181/";
//    private static final String baseURLHttp = "http://95.42.100.225:8080/";
    private static final String baseURLHttps = "https://192.168.0.103:8181/";
    private static final String baseURLHttp = "http://192.168.0.103:8080/";
//
//    private static final String baseURLHttps = "https://192.168.43.96:8181/";
//    private static final String baseURLHttp = "http://192.168.43.96:8080/";

    //base resources
    private static final String baseUser = baseURLHttps + "user/";
    private static final String baseAuth = baseURLHttps + "auth/";
    private static final String baseSync = baseURLHttps + "sync/";
    private static final String baseGoal = baseURLHttps + "goals/";
    private static final String basePeopleActivites = baseURLHttp + "people-activities/";
    private static final String baseWeight = baseURLHttps + "weights/";

    //user authentication
    public static final String googleLogin = baseAuth + "googlelogin";
    public static final String fbLogin = baseAuth + "fblogin";
    public static final String localLogin = baseAuth + "login";
    public static final String register = baseAuth + "register";
    public static final String captcha = baseURLHttp + "auth/" + "captcha";
    public static final String passwordToken = baseAuth + "forgotten-password";
    public static final String changePassword = baseAuth + "password";
    public static final String accessToken = baseAuth + "access-token";

    //user activity resources
    public static final String sportActivity = baseUser + "sport-activity";
    public static final String userSettings = baseURLHttps + "settings";
    public static final String profilePic = baseUser + "profile-pic";

    //goals
    public static final String goal = baseGoal + "goal";

    //weights
    public static final String weight = baseWeight + "weight";

    //people activities
    public static final String peopleActivities = basePeopleActivites + "activities";
    public static final String sharedMap = basePeopleActivites + "map";

    //synchronization
    public static final String checkSync = baseSync + "should-sync";
    public static final String syncActivities = baseSync + "sport-activities";
    public static final String deletedActivities = baseSync + "deleted-activities";
    public static final String insertActivities = baseSync + "insert-sport-activities";
    public static final String missingGoals = baseSync + "goals";
    public static final String deletedGoals = baseSync + "deleted-goals";
    public static final String goals = baseSync + "goals";
    public static final String weights = baseSync + "weights";
    public static final String updateSettings = baseSync + "update-settings";
    public static final String getSettings = baseSync + "settings";
    public static final String syncTimes = baseSync + "sync-times";

}



