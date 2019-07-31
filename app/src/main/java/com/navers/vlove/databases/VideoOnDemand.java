package com.navers.vlove.databases;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;

import com.navers.vlove.databases.helper.BuilderPattern;
import com.navers.vlove.databases.helper.CursorHelper;
import com.navers.vlove.databases.helper.DataTypes;
import com.navers.vlove.databases.helper.DatabaseObject;
import com.navers.vlove.databases.helper.SQLiteHelper;
import com.navers.vlove.databases.helper.TableCreator;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class VideoOnDemand extends SQLiteHelper {
    private static final String DATABASE_NAME = "VIDEO_ON_DEMAND";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_VIDEO = "VIDEO_TABLE";

    private static final String KEY_VIDEO_ID = "id";
    private static final String KEY_VIDEO_TITLE = "title";
    private static final String KEY_VIDEO_CHANNEL = "channel";
    private static final String KEY_VIDEO_COVER = "cover";
    private static final String KEY_VIDEO_SOURCE = "path";
    private static final String KEY_VIDEO_RESOLUTION = "res";
    private static final String KEY_VIDEO_CAPTION = "cap";
    private static final String KEY_VIDEO_DURATION = "dur";
    private static final String KEY_VIDEO_WIDTH = "size_w";
    private static final String KEY_VIDEO_HEIGHT = "size_h";
    private static final String KEY_VIDEO_SIZE = "file_s";
    private static final String KEY_VIDEO_VIEW_COUNT = "view_c";
    private static final String KEY_VIDEO_LIKE_COUNT = "like_c";
    private static final String KEY_VIDEO_CREATED_AT = "created_at";

    private static VideoOnDemand mInstance;

    private VideoOnDemand(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static synchronized VideoOnDemand getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new VideoOnDemand(context.getApplicationContext());
        }
        return mInstance;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        createTableVideo(sqLiteDatabase);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        TableCreator.drop(sqLiteDatabase, TABLE_VIDEO);
    }

    private void createTableVideo(SQLiteDatabase db) {
        TableCreator.with(db, TABLE_VIDEO)
                .setPrimary(KEY_VIDEO_ID, DataTypes.VARCHAR)
                .append(KEY_VIDEO_TITLE, DataTypes.VARCHAR)
                .append(KEY_VIDEO_CHANNEL, DataTypes.VARCHAR)
                .append(KEY_VIDEO_COVER, DataTypes.VARCHAR)
                .append(KEY_VIDEO_SOURCE, DataTypes.VARCHAR)
                .append(KEY_VIDEO_RESOLUTION, DataTypes.VARCHAR)
                .append(KEY_VIDEO_CAPTION, DataTypes.VARCHAR)
                .append(KEY_VIDEO_DURATION, DataTypes.DOUBLE)
                .append(KEY_VIDEO_WIDTH, DataTypes.INTEGER)
                .append(KEY_VIDEO_HEIGHT, DataTypes.INTEGER)
                .append(KEY_VIDEO_SIZE, DataTypes.LONG)
                .append(KEY_VIDEO_VIEW_COUNT, DataTypes.DOUBLE)
                .append(KEY_VIDEO_LIKE_COUNT, DataTypes.DOUBLE)
                .append(KEY_VIDEO_CREATED_AT, DataTypes.LONG)
                .create();
    }

    // All CRUD Operation.

    private boolean create(String tableName, DatabaseObject dbObject) {
        SQLiteDatabase db = getWritableDatabase();
        boolean result = db.insert(tableName, null, dbObject.convert()) != -1;
        db.close();
        return result;
    }

    private boolean read(String tableName, String keyId, String id, BuilderPattern builderPattern) {
        boolean result = false;
        SQLiteDatabase db = getWritableDatabase();

        Cursor cursor = db.query(
                tableName, null,
                keyId + " = ?",
                new String[]{id},
                null,
                null,
                null);

        if (cursor != null) {
            if (
                    cursor.getCount() == 1 &&
                            cursor.moveToFirst()
                    ) {
                builderPattern.set(cursor);
                result = true;
            }
            cursor.close();
        }

        db.close();
        return result;
    }

    private <T extends DatabaseObject> boolean readAll(String tableName, List<T> dbObjects, BuilderPattern<T> dbBuilderPatternObject) {
        boolean result = false;
        SQLiteDatabase db = getWritableDatabase();

        Cursor cursor = db.query(
                tableName,
                null,
                null,
                null,
                null,
                null,
                null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                result = true;
                do {
                    dbBuilderPatternObject.set(cursor);
                    dbObjects.add(dbBuilderPatternObject.build());
                }
                while (cursor.moveToNext());
            }
            cursor.close();
        }

        db.close();
        return result;
    }

    private boolean update(String tableName, String keyId, String id, DatabaseObject dbObject) {
        SQLiteDatabase db = getWritableDatabase();
        boolean result = db.update(tableName, dbObject.convert(), keyId + " = ?", new String[]{ id }) > 0;
        db.close();
        return result;
    }

    private boolean delete(String tableName, String keyId, String id) {
        SQLiteDatabase db = getWritableDatabase();
        boolean result = db.delete(tableName, keyId + " = ?", new String[]{ id }) > 0;
        db.close();
        return result;
    }

    private boolean createVideo(Video video) {
        return create(TABLE_VIDEO, video);
    }

    private boolean readVideo(String id, Video.Builder builder) {
        return read(TABLE_VIDEO, KEY_VIDEO_ID, id, builder);
    }

    private boolean readAllVideo(List<Video> videos) {
        return readAll(TABLE_VIDEO, videos, new Video.Builder());
    }

    private boolean updateVideo(Video video) {
        return update(TABLE_VIDEO, KEY_VIDEO_ID, video.id, video);
    }

    private boolean deleteVideo(Video video) {
        return delete(TABLE_VIDEO, KEY_VIDEO_ID, video.id);
    }

    /**
     * All Visible CRUD Operation.
     */
    public boolean add(Video video) {
        return createVideo(video);
    }

    public List<Video> get() {
        List<Video> videos = new ArrayList<>();
        readAllVideo(videos);
        return videos;
    }

    public Video get(String id) {
        Video.Builder builder = new Video.Builder();
        if (readVideo(id, builder)) {
            return builder.build();
        }
        else {
            return null;
        }
    }

    public boolean update(Video video) {
        return updateVideo(video);
    }

    public boolean delete(Video video) {
        return deleteVideo(video);
    }

    public int size() {
        return (int) DatabaseUtils.queryNumEntries(getReadableDatabase(), TABLE_VIDEO);
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    public boolean contain(String postId) {
        return get(postId) != null;
    }

    @Override
    public synchronized void close() {
        super.close();
        if (mInstance != null) {
            mInstance = null;
        }
    }

//    public static class VIDEO implements DatabaseObject, Parcelable {
//        public static final Parcelable.Creator<VIDEO> CREATOR = new Parcelable.Creator<VIDEO>() {
//
//            @Override
//            public VIDEO createFromParcel(Parcel source) {
//                return new VIDEO(source);
//            }
//
//            @Override
//            public VIDEO[] newArray(int size) {
//                return new VIDEO[size];
//            }
//        };
//
//        private String id;
//        private String title;
//        private String channel;
//        private String cover;
//        private String source;
//        private String resolution;
//        private String caption;
//        private double duration;
//        private int width;
//        private int height;
//        private long size;
//        private double viewCount;
//        private double likeCount;
//        private long createdAt;
//
//        VIDEO(Parcel source) {
//            source.readString();
//            source.readString();
//            source.readString();
//            source.readString();
//            source.readString();
//            source.readString();
//            source.readString();
//            source.readDouble();
//            source.readInt();
//            source.readInt();
//            source.readLong();
//            source.readDouble();
//            source.readDouble();
//            source.readLong();
//        }
//
//        VIDEO(String id) {
//            this.id = id;
//        }
//
//        public String getTitle() {
//            return title;
//        }
//
//        public String getChannel() {
//            return channel;
//        }
//
//        public String getCover() {
//            return cover;
//        }
//
//        public String getSource() {
//            return source;
//        }
//
//        public String getResolution() {
//            return resolution;
//        }
//
//        public String getCaption() {
//            return caption;
//        }
//
//        public double getDuration() {
//            return duration;
//        }
//
//        public int getWidth() {
//            return width;
//        }
//
//        public int getHeight() {
//            return height;
//        }
//
//        public long getSize() {
//            return size;
//        }
//
//        public double getViewCount() {
//            return viewCount;
//        }
//
//        public double getLikeCount() {
//            return likeCount;
//        }
//
//        public long getCreatedAt() {
//            return createdAt;
//        }
//
//        @Override
//        public int describeContents() {
//            return 0;
//        }
//
//        @Override
//        public void writeToParcel(Parcel dest, int flags) {
//            dest.writeString(id);
//            dest.writeString(title);
//            dest.writeString(channel);
//            dest.writeString(cover);
//            dest.writeString(source);
//            dest.writeString(resolution);
//            dest.writeString(caption);
//            dest.writeDouble(duration);
//            dest.writeInt(width);
//            dest.writeInt(height);
//            dest.writeLong(size);
//            dest.writeDouble(viewCount);
//            dest.writeDouble(likeCount);
//            dest.writeLong(createdAt);
//        }
//
//        @Override
//        public ContentValues convert() {
//            ContentValues values = new ContentValues();
//
//            values.put(KEY_VIDEO_ID, id);
//            values.put(KEY_VIDEO_TITLE, title);
//            values.put(KEY_VIDEO_CHANNEL, channel);
//            values.put(KEY_VIDEO_COVER, cover);
//            values.put(KEY_VIDEO_SOURCE, source);
//            values.put(KEY_VIDEO_RESOLUTION, resolution);
//            values.put(KEY_VIDEO_CAPTION, caption);
//            values.put(KEY_VIDEO_DURATION, duration);
//            values.put(KEY_VIDEO_WIDTH, width);
//            values.put(KEY_VIDEO_HEIGHT, height);
//            values.put(KEY_VIDEO_SIZE, size);
//            values.put(KEY_VIDEO_VIEW_COUNT, viewCount);
//            values.put(KEY_VIDEO_LIKE_COUNT, likeCount);
//            values.put(KEY_VIDEO_CREATED_AT, createdAt);
//
//            return values;
//        }
//
//        public static class Builder implements BuilderPattern<VIDEO> {
//            private String id;
//            private String title;
//            private String channel;
//            private String cover;
//            private String source;
//            private String resolution;
//            private String caption;
//            private double duration;
//            private int width;
//            private int height;
//            private long size;
//            private double viewCount;
//            private double likeCount;
//            private long createdAt;
//
//            public static Builder copy(VIDEO video) {
//                Builder builder = new Builder(video.id);
//
//                builder.title = video.title;
//                builder.channel = video.channel;
//                builder.cover = video.cover;
//                builder.source = video.source;
//                builder.resolution = video.resolution;
//                builder.caption = video.caption;
//                builder.duration = video.duration;
//                builder.width = video.width;
//                builder.height = video.height;
//                builder.size = video.size;
//                builder.viewCount = video.viewCount;
//                builder.likeCount = video.likeCount;
//                builder.createdAt = video.createdAt;
//
//                return builder;
//            }
//
//            protected static Builder with(Cursor cursor) {
//                Builder builder = new Builder();
//                builder.set(cursor);
//                return builder;
//            }
//
//            private static String buildKey(String key) {
//                return TABLE_VIDEO + "." + key;
//            }
//
//            private Builder() {}
//
//            public Builder(String id) {
//                this.id = id;
//                this.createdAt = System.currentTimeMillis();
//            }
//
//            public Builder setTitle(String title) {
//                this.title = title;
//                return this;
//            }
//
//            public Builder setChannel(String channel) {
//                this.channel = channel;
//                return this;
//            }
//
//            public Builder setCover(String cover) {
//                this.cover = cover;
//                return this;
//            }
//
//            public Builder setSource(String source) {
//                this.source = source;
//                return this;
//            }
//
//            public Builder setResolution(String resolution) {
//                this.resolution = resolution;
//                return this;
//            }
//
//            public Builder setCaption(String caption) {
//                this.caption = caption;
//                return this;
//            }
//
//            public Builder setDuration(double duration) {
//                this.duration = duration;
//                return this;
//            }
//
//            public Builder setWidth(int width) {
//                this.width = width;
//                return this;
//            }
//
//            public Builder setHeight(int height) {
//                this.height = height;
//                return this;
//            }
//
//            public Builder setSize(long size) {
//                this.size = size;
//                return this;
//            }
//
//            public Builder setViewCount(double viewCount) {
//                this.viewCount = viewCount;
//                return this;
//            }
//
//            public Builder setLikeCount(double likeCount) {
//                this.likeCount = likeCount;
//                return this;
//            }
//
//            public Builder setCreatedAt(long createdAt) {
//                this.createdAt = createdAt;
//                return this;
//            }
//
//            @Override
//            public void set(Cursor cursor) {
//                id = CursorHelper.getStringValue(cursor, buildKey(KEY_VIDEO_ID));
//                setTitle(CursorHelper.getStringValue(cursor, buildKey(KEY_VIDEO_TITLE)));
//                setChannel(CursorHelper.getStringValue(cursor, buildKey(KEY_VIDEO_CHANNEL)));
//                setCover(CursorHelper.getStringValue(cursor, buildKey(KEY_VIDEO_COVER)));
//                setSource(CursorHelper.getStringValue(cursor, buildKey(KEY_VIDEO_SOURCE)));
//                setResolution(CursorHelper.getStringValue(cursor, buildKey(KEY_VIDEO_RESOLUTION)));
//                setCaption(CursorHelper.getStringValue(cursor, buildKey(KEY_VIDEO_CAPTION)));
//                setDuration(CursorHelper.getDoubleValue(cursor, buildKey(KEY_VIDEO_DURATION)));
//                setWidth(CursorHelper.getIntValue(cursor, buildKey(KEY_VIDEO_WIDTH)));
//                setHeight(CursorHelper.getIntValue(cursor, buildKey(KEY_VIDEO_HEIGHT)));
//                setSize(CursorHelper.getLongValue(cursor, buildKey(KEY_VIDEO_SIZE)));
//                setViewCount(CursorHelper.getDoubleValue(cursor, buildKey(KEY_VIDEO_VIEW_COUNT)));
//                setLikeCount(CursorHelper.getDoubleValue(cursor, buildKey(KEY_VIDEO_LIKE_COUNT)));
//                setCreatedAt(CursorHelper.getLongValue(cursor, buildKey(KEY_VIDEO_CREATED_AT)));
//            }
//
//            @Override
//            public VIDEO build() {
//                VIDEO video = new VIDEO(id);
//
//                video.title = title;
//                video.channel = channel;
//                video.cover = cover;
//                video.source = source;
//                video.resolution = resolution;
//                video.caption = caption;
//                video.duration = duration;
//                video.width = width;
//                video.height = height;
//                video.size = size;
//                video.viewCount = viewCount;
//                video.likeCount = likeCount;
//                video.createdAt = createdAt;
//
//                return video;
//            }
//        }
//    }

    public static class Video implements DatabaseObject, Serializable {
        /**
         *
         */
        private static final long serialVersionUID = 1092598135964608767L;

        private String id;
        private String title;
        private String channel;
        private String cover;
        private String source;
        private String resolution;
        private String caption;
        private double duration;
        private int width;
        private int height;
        private long size;
        private double viewCount;
        private double likeCount;
        private long createdAt;

        private Video(String id) {
            this.id = id;
        }

        private Video() {}

        @Override
        public ContentValues convert() {
            ContentValues values = new ContentValues();

            values.put(KEY_VIDEO_ID, id);
            values.put(KEY_VIDEO_TITLE, title);
            values.put(KEY_VIDEO_CHANNEL, channel);
            values.put(KEY_VIDEO_COVER, cover);
            values.put(KEY_VIDEO_SOURCE, source);
            values.put(KEY_VIDEO_RESOLUTION, resolution);
            values.put(KEY_VIDEO_CAPTION, caption);
            values.put(KEY_VIDEO_DURATION, duration);
            values.put(KEY_VIDEO_WIDTH, width);
            values.put(KEY_VIDEO_HEIGHT, height);
            values.put(KEY_VIDEO_SIZE, size);
            values.put(KEY_VIDEO_VIEW_COUNT, viewCount);
            values.put(KEY_VIDEO_LIKE_COUNT, likeCount);
            values.put(KEY_VIDEO_CREATED_AT, createdAt);

            return values;
        }

        public String getTitle() {
            return title;
        }

        public String getChannel() {
            return channel;
        }

        public String getCover() {
            return cover;
        }

        public String getSource() {
            return source;
        }

        public String getResolution() {
            return resolution;
        }

        public String getCaption() {
            return caption;
        }

        public double getDuration() {
            return duration;
        }

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }

        public long getSize() {
            return size;
        }

        public double getViewCount() {
            return viewCount;
        }

        public double getLikeCount() {
            return likeCount;
        }

        public long getCreatedAt() {
            return createdAt;
        }

        public static class Builder implements BuilderPattern<Video> {
            private String id;
            private String title;
            private String channel;
            private String cover;
            private String source;
            private String resolution;
            private String caption;
            private double duration;
            private int width;
            private int height;
            private long size;
            private double viewCount;
            private double likeCount;
            private long createdAt;

            public static Builder copy(Video video) {
                Builder builder = new Builder(video.id);
                builder.title = video.title;
                builder.channel = video.channel;
                builder.cover = video.cover;
                builder.source = video.source;
                builder.resolution = video.resolution;
                builder.caption = video.caption;
                builder.duration = video.duration;
                builder.width = video.width;
                builder.height = video.height;
                builder.size = video.size;
                builder.viewCount = video.viewCount;
                builder.likeCount = video.likeCount;
                builder.createdAt = video.createdAt;
                return builder;
            }

            protected static Builder with(Cursor cursor) {
                Builder builder = new Builder();
                builder.set(cursor);
                return builder;
            }

            private static String buildKey(String key) {
                return TABLE_VIDEO + "." + key;
            }

            private Builder() {}

            public Builder(String id) {
                this.id = id;
                this.createdAt = System.currentTimeMillis();
            }

            public Builder setTitle(String title) {
                this.title = title;
                return this;
            }

            public Builder setChannel(String channel) {
                this.channel = channel;
                return this;
            }

            public Builder setCover(String cover) {
                this.cover = cover;
                return this;
            }

            public Builder setSource(String source) {
                this.source = source;
                return this;
            }

            public Builder setResolution(String resolution) {
                this.resolution = resolution;
                return this;
            }

            public Builder setCaption(String caption) {
                this.caption = caption;
                return this;
            }

            public Builder setDuration(double duration) {
                this.duration = duration;
                return this;
            }

            public Builder setWidth(int width) {
                this.width = width;
                return this;
            }

            public Builder setHeight(int height) {
                this.height = height;
                return this;
            }

            public Builder setSize(long size) {
                this.size = size;
                return this;
            }

            public Builder setViewCount(double viewCount) {
                this.viewCount = viewCount;
                return this;
            }

            public Builder setLikeCount(double likeCount) {
                this.likeCount = likeCount;
                return this;
            }

            public Builder setCreatedAt(long createdAt) {
                this.createdAt = createdAt;
                return this;
            }

            @Override
            public void set(Cursor cursor) {
                id = CursorHelper.getStringValue(cursor, buildKey(KEY_VIDEO_ID));
                setTitle(CursorHelper.getStringValue(cursor, buildKey(KEY_VIDEO_TITLE)));
                setChannel(CursorHelper.getStringValue(cursor, buildKey(KEY_VIDEO_CHANNEL)));
                setCover(CursorHelper.getStringValue(cursor, buildKey(KEY_VIDEO_COVER)));
                setSource(CursorHelper.getStringValue(cursor, buildKey(KEY_VIDEO_SOURCE)));
                setResolution(CursorHelper.getStringValue(cursor, buildKey(KEY_VIDEO_RESOLUTION)));
                setCaption(CursorHelper.getStringValue(cursor, buildKey(KEY_VIDEO_CAPTION)));
                setDuration(CursorHelper.getDoubleValue(cursor, buildKey(KEY_VIDEO_DURATION)));
                setWidth(CursorHelper.getIntValue(cursor, buildKey(KEY_VIDEO_WIDTH)));
                setHeight(CursorHelper.getIntValue(cursor, buildKey(KEY_VIDEO_HEIGHT)));
                setSize(CursorHelper.getLongValue(cursor, buildKey(KEY_VIDEO_SIZE)));
                setViewCount(CursorHelper.getDoubleValue(cursor, buildKey(KEY_VIDEO_VIEW_COUNT)));
                setLikeCount(CursorHelper.getDoubleValue(cursor, buildKey(KEY_VIDEO_LIKE_COUNT)));
                setCreatedAt(CursorHelper.getLongValue(cursor, buildKey(KEY_VIDEO_CREATED_AT)));
            }

            @Override
            public Video build() {
                Video video = new Video(id);
                video.title = title;
                video.channel = channel;
                video.cover = cover;
                video.source = source;
                video.resolution = resolution;
                video.caption = caption;
                video.duration = duration;
                video.width = width;
                video.height = height;
                video.size = size;
                video.viewCount = viewCount;
                video.likeCount = likeCount;
                video.createdAt = createdAt;
                return video;
            }
        }
    }

    public static class Caption implements Serializable {
        /**
         *
         */
        private static final long serialVersionUID = -3186869485207938272L;

        private String label;
        private String language;
        private String locale;
        private String country;
        private String source;
        private String category;

        private Caption() {}

        public String getLabel() {
            return label;
        }

        public String getLanguage() {
            return language;
        }

        public String getLocale() {
            return locale;
        }

        public String getCountry() {
            return country;
        }

        public String getSource() {
            return source;
        }

        public String getCategory() {
            return category;
        }

        public static class Builder implements BuilderPattern<Caption> {
            private String label;
            private String language;
            private String locale;
            private String country;
            private String source;
            private String category;

            public Builder setLabel(String label) {
                this.label = label;
                return this;
            }

            public Builder setLanguage(String language) {
                this.language = language;
                return this;
            }

            public Builder setLocale(String locale) {
                this.locale = locale;
                return this;
            }

            public Builder setCountry(String country) {
                this.country = country;
                return this;
            }

            public Builder setSource(String source) {
                this.source = source;
                return this;
            }

            public Builder setCategory(String category) {
                this.category = category;
                return this;
            }

            @Override
            public void set(Cursor cursor) {

            }

            @Override
            public Caption build() {
                Caption caption = new Caption();
                caption.label = label;
                caption.language = language;
                caption.locale = locale;
                caption.country = country;
                caption.source = source;
                caption.category = category;
                return caption;
            }
        }
    }
}
