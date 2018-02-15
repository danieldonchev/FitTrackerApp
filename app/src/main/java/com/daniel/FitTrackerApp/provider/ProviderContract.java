package com.daniel.FitTrackerApp.provider;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public final class ProviderContract
{
    private ProviderContract(){}

    /**
     * Content provider authority.
     */
    public static final String CONTENT_AUTHORITY = "com.example.daniel.gmapp";
    public static final String CONTENT_APP_BASE = "gmapp.";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String CONTENT_TYPE_BASE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd." + CONTENT_APP_BASE;

    public static final String CONTENT_ITEM_TYPE_BASE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd." + CONTENT_APP_BASE;

    private static final String PATH_SPORT_ACTIVITY_ENTRY = "sport_activity_entry";
    private static final String PATH_ACCOUNT_ENTRY = "account_entry";
    private static final String PATH_SYNC_ENTRY = "sync_entry";
    private static final String PATH_SPORT_ACTIVITY_SPLIT_ENTRY = "sport_activity_split";
    private static final String PATH_GOAL_ENTRY = "goal_entry";
    private static final String PATH_WEIGHT_ENTRY = "weight_entry";

    public interface SportActivityColumns extends BaseColumns
    {
        String ACCOUNT_ID = "user_id";
        String ACTIVITY = "activity";
        String DISTANCE = "distance";
        String DURATION = "duration";
        String STEPS = "steps";
        String CALORIES = "calories";
        String MAPDATA = "map_data";
        String START_TIMESTAMP = "start_time";
        String END_TIMESTAMP = "end_time";
        String TYPE = "type";
        String LAST_MODIFIED = "last_modified";
        String DELETED = "deleted";
        String SYNCED = "synced";
    }

    public interface SplitColumns
    {
        String SPLIT_ID = "_id";
        String SPLIT_ACTIVITY_ID = "sport_activity_id";
        String SPLIT_ACCOUNT_ID = "user_id";
        String SPLIT_DISTANCE = "distance";
        String SPLIT_DURATION = "duration";
    }

    public static class SportActivityEntry implements SportActivityColumns, SplitColumns {

        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + CONTENT_TYPE_BASE + "sport_activities";

        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + CONTENT_TYPE_BASE + "sport_activity";

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_SPORT_ACTIVITY_ENTRY).build();
        public static final Uri SPLIT_CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_SPORT_ACTIVITY_SPLIT_ENTRY).build();

        public static final String CONTENT_TYPE_ID = "sport_activity";

        public static final String TABLE_NAME = "activities";
        public static final String SPLIT_TABLE_NAME = "activity_spits";

        public static final String FULL_PROJECTION[] = {_ID,
                ACTIVITY,
                SportActivityColumns.DISTANCE,
                SportActivityColumns.DURATION,
                STEPS,
                CALORIES,
                MAPDATA,
                START_TIMESTAMP,
                END_TIMESTAMP,
                TYPE,
                LAST_MODIFIED};

        /** Build {@link Uri} for requested {@link #_ID}. */
        public static String getSportActivityId(Uri uri) {
            return uri.getPathSegments().get(1);
        }
    }

    public interface AccountColumns extends BaseColumns {
        String EMAIL = "email";
        String SETTINGS = "settings";
        String UPDATED = "updateStatus";
    }

    public static class AccountEntry implements AccountColumns {
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + CONTENT_TYPE_BASE + "accounts";

        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + CONTENT_TYPE_BASE + "account";

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_ACCOUNT_ENTRY).build();

        public static final String CONTENT_TYPE_ID = "account";

        public static final String TABLE_NAME = "accounts";
    }

    public interface SyncColumns extends BaseColumns {
        String LAST_SYNC = "last_sync";
        String LAST_MODIFIED = "last_modified";
        String LAST_MODIFIED_ACTIVITIES = "last_modified_activities";
        String LAST_MODIFIED_SETTINGS = "last_modified_settings";
        String LAST_MODIFIED_GOALS = "last_modified_goals";
        String LAST_MODIFIED_WEIGHTS = "last_modified_weights";
    }

    public static class SyncEntry implements SyncColumns {
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + CONTENT_TYPE_BASE + "syncs";

        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + CONTENT_TYPE_BASE + "sync";

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_SYNC_ENTRY).build();

        public static final String CONTENT_TYPE_ID = "sync";

        public static final String TABLE_NAME = "Syncs";
    }

    public interface GoalColumns extends BaseColumns {
        String ACCOUNT_ID = "user_id";
        String TYPE = "type";
        String DISTANCE = "distance";
        String DURATION = "duration";
        String CALORIES = "calories";
        String STEPS = "steps";
        String FROM_DATE = "from_date";
        String TO_DATE = "to_date";
        String DELETED = "deleted";
        String LAST_MODIFIED = "last_modified";
        String SYNCED = "synced";
    }

    public static class GoalEntry implements GoalColumns{
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + CONTENT_TYPE_BASE + "goals";

        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + CONTENT_TYPE_BASE + "goal";

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_GOAL_ENTRY).build();

        public static final String CONTENT_TYPE_ID = "goal";

        public static final String TABLE_NAME = "goals";

        public static String[] FULL_PROJECTION = {_ID,
                                                TYPE,
                                                DISTANCE,
                                                DURATION,
                                                CALORIES,
                                                STEPS,
                                                FROM_DATE,
                                                TO_DATE,
                                                LAST_MODIFIED};
    }

    public interface WeightColumns {
        String DATE = "date";
        String ACCOUNT_ID = "user_id";
        String WEIGHT = "weight";
        String LAST_MODIFIED = "last_modified";
        String SYNCED = "synced";
    }

    public static class WeightEntry implements WeightColumns{
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + CONTENT_TYPE_BASE + "weights";

        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + CONTENT_TYPE_BASE + "weight";

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_WEIGHT_ENTRY).build();

        public static final String CONTENT_TYPE_ID = "weight";

        public static final String TABLE_NAME = "weights";

        public static String[] FULL_PROJECTION = {DATE,
                ACCOUNT_ID,
                WEIGHT,
                LAST_MODIFIED};
    }
}
