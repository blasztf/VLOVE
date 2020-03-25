package com.doodlyz.vlove.databases;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;

import com.doodlyz.vlove.databases.helper.BuilderPattern;
import com.doodlyz.vlove.databases.helper.CursorHelper;
import com.doodlyz.vlove.databases.helper.DataTypes;
import com.doodlyz.vlove.databases.helper.DatabaseObject;
import com.doodlyz.vlove.databases.helper.SQLiteHelper;
import com.doodlyz.vlove.databases.helper.TableCreator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Board extends SQLiteHelper {
    public static final int LIMIT = 5;
    public static final String MARK_DELETED = "(=+-^deleted^-+=)";

    private static final String DATABASE_NAME = "BOARD";
    private static final int DATABASE_VERSION = 2;

    private static final String TABLE_POST = "POST";
    private static final String TABLE_COMMENT = "COMMENT";

    private static final String KEY_POST_ID = "post_id";
    private static final String KEY_POST_USER = "post_user";
    private static final String KEY_POST_USER_IMAGE = "post_user_image";
    private static final String KEY_POST_CONTENT = "post_content";
    private static final String KEY_POST_TOTAL_COMMENT = "post_comment_total";
    private static final String KEY_POST_CHANNEL_CODE = "post_channel_code";
    private static final String KEY_POST_CREATED_AT = "post_created_at";
    private static final String KEY_POST_LAST_UPDATED = "post_last_updated";

    private static final String KEY_COMMENT_ID = "comment_id";
    private static final String KEY_COMMENT_USER = "comment_user";
    private static final String KEY_COMMENT_USER_IMAGE = "comment_user_image";
    private static final String KEY_COMMENT_CONTENT = "comment_content";
    private static final String KEY_COMMENT_TOTAL_COMMENT = "comment_comment_total";
    private static final String KEY_COMMENT_POST_ID = "comment_post_id";
    private static final String KEY_COMMENT_STICKER_ID = "comment_sticker_id";
    private static final String KEY_COMMENT_STICKER_PACK = "comment_sticker_pack_code";
    private static final String KEY_COMMENT_CREATED_AT = "comment_created_at";
    private static final String KEY_COMMENT_LAST_UPDATED = "comment_last_updated";

    private static Board mInstance;

    private Board(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static synchronized Board getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new Board(context.getApplicationContext());
        }
        return mInstance;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        createTablePosts(sqLiteDatabase);
        createTableComments(sqLiteDatabase);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            alterTablePosts(db, KEY_POST_CHANNEL_CODE);
        }
    }

    private void createTablePosts(SQLiteDatabase db) {
        TableCreator.with(db, TABLE_POST)
                .setPrimary(KEY_POST_ID, DataTypes.VARCHAR)
                .append(KEY_POST_USER, DataTypes.VARCHAR)
                .append(KEY_POST_USER_IMAGE, DataTypes.VARCHAR)
                .append(KEY_POST_CONTENT, DataTypes.TEXT)
                .append(KEY_POST_TOTAL_COMMENT, DataTypes.INTEGER)
                .append(KEY_POST_CHANNEL_CODE, DataTypes.VARCHAR)
                .append(KEY_POST_CREATED_AT, DataTypes.LONG)
                .append(KEY_POST_LAST_UPDATED, DataTypes.LONG)
                .create();
    }

    private void createTableComments(SQLiteDatabase db) {
        TableCreator.with(db, TABLE_COMMENT)
                .setPrimary(KEY_COMMENT_ID, DataTypes.VARCHAR)
                .append(KEY_COMMENT_USER, DataTypes.VARCHAR)
                .append(KEY_COMMENT_USER_IMAGE, DataTypes.VARCHAR)
                .append(KEY_COMMENT_CONTENT, DataTypes.TEXT)
                .append(KEY_COMMENT_TOTAL_COMMENT, DataTypes.INTEGER)
                .appendForeign(KEY_COMMENT_POST_ID, DataTypes.VARCHAR, TABLE_POST, KEY_POST_ID, true)
                .append(KEY_COMMENT_STICKER_ID, DataTypes.VARCHAR)
                .append(KEY_COMMENT_STICKER_PACK, DataTypes.VARCHAR)
                .append(KEY_COMMENT_CREATED_AT, DataTypes.LONG)
                .append(KEY_COMMENT_LAST_UPDATED, DataTypes.LONG)
                .create();
    }

    private void alterTablePosts(SQLiteDatabase db, String column) {
        db.execSQL("ALTER TABLE " + TABLE_POST + " ADD COLUMN " + column + " VARCHAR");
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

    private boolean createPost(Post post) {
        return create(TABLE_POST, post);
    }

    private boolean createComment(Comment comment) {
        return create(TABLE_COMMENT, comment);
    }

    private boolean updatePost(Post post) {
        return update(TABLE_POST, KEY_POST_ID, post.getId(), post);
    }

    private boolean updateComment(Comment comment) {
        return update(TABLE_COMMENT, KEY_COMMENT_ID, comment.getId(), comment);
    }

    private boolean deletePost(Post post) {
        return delete(TABLE_POST, KEY_POST_ID, post.getId());
    }

    private boolean deleteComment(Comment comment) {
        return delete(TABLE_COMMENT, KEY_COMMENT_ID, comment.getId());
    }

    /**
     * All Visible CRUD Operation.
     */
    public boolean add(Post post) {
        boolean result = createPost(post);
        if (result) {
            List<Comment> comments = post.getComments();
            for (Comment comment : comments) {
                if (!createComment(comment)) {
                    result = false;
                }
            }
        }
        return result;
    }

    public List<Post> retrieve() {
        Cursor cursorPost, cursorComment;

        List<Post> posts = new ArrayList<>();
        List<Comment> comments;

        SQLiteDatabase db = getWritableDatabase();

        cursorPost = db.query(TABLE_POST, null, null, null, null, null, TABLE_POST + "." + KEY_POST_LAST_UPDATED + " DESC");
        if (cursorPost != null) {
            if (cursorPost.moveToFirst()) {
                Post post;
                Comment comment;
                do {
                    post = Post.Builder.with(cursorPost).build();
                    comments = new ArrayList<>();
                    cursorComment = db.query(TABLE_COMMENT, null, TABLE_COMMENT + "." + KEY_COMMENT_POST_ID + " = " + post.getId(), null, null, null, TABLE_COMMENT + "." + KEY_COMMENT_LAST_UPDATED + " DESC");
                    if (cursorComment != null) {
                        if (cursorComment.moveToFirst()) {
                            do {
                                comment = Comment.Builder.with(cursorComment).build();
                                comments.add(comment);
                            }
                            while (cursorComment.moveToNext());
                        }
                        cursorComment.close();
                    }
                    post.setComments(comments);
                    posts.add(post);
                }
                while (cursorPost.moveToNext());
            }
            cursorPost.close();
        }

        db.close();

        return posts;
    }

    public Post retrieve(String id) {
        Cursor cursorPost, cursorComment;
        Post post = null;

        SQLiteDatabase db = getWritableDatabase();

        cursorPost = db.query(TABLE_POST, null, TABLE_POST + "." + KEY_POST_ID + " = " + id, null, null, null, TABLE_POST + "." + KEY_POST_LAST_UPDATED + " DESC");

        if (cursorPost != null) {
            if (cursorPost.moveToFirst()) {
                post = Post.Builder.with(cursorPost).build();

                List<Comment> comments = new ArrayList<>();
                Comment comment;

                cursorComment = db.query(TABLE_COMMENT, null, TABLE_COMMENT + "." + KEY_COMMENT_POST_ID + " = " + post.getId(), null, null, null, TABLE_COMMENT + "." + KEY_COMMENT_LAST_UPDATED + " DESC");
                if (cursorComment != null) {
                    if (cursorComment.moveToFirst()) {
                        do {
                            comment = Comment.Builder.with(cursorComment).build();
                            comments.add(comment);
                        }
                        while (cursorComment.moveToNext());
                    }
                    cursorComment.close();
                }

                post.setComments(comments);
            }
            cursorPost.close();
        }

        db.close();

        return post;
    }

    @Deprecated
    public List<Post> get() {
        List<Post> posts = new ArrayList<>();
        List<Comment> comments = new ArrayList<>();

        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + TABLE_COMMENT + " INNER JOIN " + TABLE_POST +
                        " ON " + TABLE_COMMENT + "." + KEY_COMMENT_POST_ID + " = " + TABLE_POST + "." + KEY_POST_ID +
                        " ORDER BY " + TABLE_POST + "." + KEY_POST_ID + " DESC;",
                null,
                null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                Post post;
                Comment comment;
                boolean check;

                do {
                    post = Post.Builder.with(cursor).build();
                    if (posts.isEmpty()) {
                        posts.add(post);
                    }

                    check = !posts.get(posts.size() - 1).getId().equals(post.getId());
                    if (check) {
                        posts.get(posts.size() - 1).setComments(comments);
                        posts.add(post);
                        comments = new ArrayList<>();
                    } else {
                        comment = Comment.Builder.with(cursor).build();
                        comments.add(comment);
                    }
                }
                while (cursor.moveToNext());
                posts.get(posts.size() - 1).setComments(comments);
            }

            cursor.close();
        }

        db.close();

        return posts;
    }

    @Deprecated
    public Post get(String postId) {
        Post post = null;
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + TABLE_COMMENT + " INNER JOIN " + TABLE_POST +
                        " ON " + TABLE_COMMENT + "." + KEY_COMMENT_POST_ID + " = " + TABLE_POST + "." + KEY_POST_ID +
                        " WHERE " + TABLE_POST + "." + KEY_POST_ID + " = " + postId + ";",
                null,
                null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                post = Post.Builder.with(cursor).build();
                List<Comment> comments = new ArrayList<>();

                Comment comment;
                do {
                    comment = Comment.Builder.with(cursor).build();
                    comments.add(comment);
                }
                while (cursor.moveToNext());

                post.setComments(comments);
            }
            cursor.close();
        }

        db.close();
        return post;
    }

    public boolean update(Post post) {
        boolean result = updatePost(post);
        if (result) {
            List<Comment> comments = post.getComments();
            for (Comment comment : comments) {
                if (comment.getId().equals(Board.MARK_DELETED)) {
                    comment.setId(comment.getContent());
                    deleteComment(comment);
                }
                else {
                    if (!updateComment(comment)) {
                        if (!createComment(comment)) {
                            result = false;
                        }
                    }
                }
            }
        }
        return result;
    }

    public boolean delete(Post post) {
        return deletePost(post);
    }

    public int size() {
        return (int) DatabaseUtils.queryNumEntries(getReadableDatabase(), TABLE_POST);
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    public boolean contain(String postId) {
        return retrieve(postId) != null;
    }

    public boolean isExceed() {
        return size() >= Board.LIMIT;
    }

    @Deprecated
    public String testQuery(String query) {
        String result = "";
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.rawQuery(
                query,
                null,
                null);
        if (cursor != null) {
            result += "query executed!\n";
            result += "total row: " + cursor.getCount() + "\n\n";

            int j = 0;
            if (cursor.moveToFirst()) {
                do {
                    result += "row " + j + ":\n\n";
                    for (int i = 0, l = cursor.getColumnCount(); i < l; i++) {
                        result += "\t" + cursor.getColumnName(i) + ": " + getValue(cursor, i) + "\n\n";
                    }
                    j++;
                }
                while (cursor.moveToNext());
            }
        }
        else {
            result += "query failed";
        }

        return result;
    }

    @Deprecated
    private Object getValue(Cursor cursor, int i) {
        switch (cursor.getType(i)) {
            case Cursor.FIELD_TYPE_STRING:
                return cursor.getString(i);
            case Cursor.FIELD_TYPE_INTEGER:
                return cursor.getDouble(i);
            case Cursor.FIELD_TYPE_FLOAT:
                return cursor.getFloat(i);
                default:
                    return cursor.getBlob(i);
        }
    }

    @Override
    public synchronized void close() {
        super.close();
        if (mInstance != null) {
            mInstance = null;
        }
    }

    /**
     * POJO Post
     */
    public static class Post implements DatabaseObject {
        private String id;
        private String user;
        private String userImage;
        private String content;
        private int totalComment;
        private List<Comment> comments = new ArrayList<>();
        private String channelCode;
        private long createdAt;
        private long lastUpdated;

        public static class Builder implements BuilderPattern<Post> {
            private String id;
            private String user;
            private String userImage;
            private String content;
            private int totalComment;
            private String channelCode;
            private long createdAt;
            private long lastUpdated;

            protected static Builder with(Cursor cursor) {
                Builder builder = new Builder();
                builder.set(cursor);
                return builder;
            }

            private Builder() {}

            public Builder(String id) {
                setId(id);
            }

            @Override
            public void set(Cursor cursor) {
                setId(CursorHelper.getStringValue(cursor, KEY_POST_ID));
                setUser(CursorHelper.getStringValue(cursor, KEY_POST_USER));
                setUserImage(CursorHelper.getStringValue(cursor, KEY_POST_USER_IMAGE));
                setContent(CursorHelper.getStringValue(cursor, KEY_POST_CONTENT));
                setTotalComment(CursorHelper.getIntValue(cursor, KEY_POST_TOTAL_COMMENT));
                setChannelCode(CursorHelper.getStringValue(cursor, KEY_POST_CHANNEL_CODE));
                setCreatedAt(CursorHelper.getLongValue(cursor, KEY_POST_CREATED_AT));
                setLastUpdated(CursorHelper.getLongValue(cursor, KEY_POST_LAST_UPDATED));
            }

            private Builder setId(String id) {
                this.id = id;
                return this;
            }

            public Builder setUser(String user) {
                this.user = user;
                return this;
            }

            public Builder setUserImage(String userImage) {
                this.userImage = userImage;
                return this;
            }

            public Builder setContent(String content) {
                this.content = content;
                return this;
            }

            public Builder setTotalComment(int totalComment) {
                this.totalComment = totalComment;
                return this;
            }

            public Builder setChannelCode(String channelCode) {
                this.channelCode = channelCode;
                return this;
            }

            public Builder setCreatedAt(long createdAt) {
                this.createdAt = createdAt;
                return this;
            }

            public Builder setLastUpdated(long lastUpdated) {
                this.lastUpdated = lastUpdated;
                return this;
            }

            @Override
            public Post build() {
                Post post = new Post(id);
                post.setUser(user);
                post.setUserImage(userImage);
                post.setContent(content);
                post.setTotalComment(totalComment);
                post.setChannelCode(channelCode);
                post.setCreatedAt(createdAt);
                post.setLastUpdated(lastUpdated);
                return post;
            }
        }

        private Post(String id) {
            setId(id);
            setLastUpdated(System.currentTimeMillis());
        }

        private Post() { }

        @Override
        public ContentValues convert() {
            ContentValues values = new ContentValues();

            values.put(KEY_POST_ID, getId());
            values.put(KEY_POST_USER, getUser());
            values.put(KEY_POST_USER_IMAGE, getUserImage());
            values.put(KEY_POST_CONTENT, getContent());
            values.put(KEY_POST_TOTAL_COMMENT, getTotalComment());
            values.put(KEY_POST_CHANNEL_CODE, getChannelCode());
            values.put(KEY_POST_CREATED_AT, getCreatedAt());
            values.put(KEY_POST_LAST_UPDATED, getLastUpdated());

            return values;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getUser() {
            return user;
        }

        public void setUser(String user) {
            this.user = user;
        }

        public String getUserImage() {
            return userImage;
        }

        public void setUserImage(String userImage) {
            this.userImage = userImage;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public int getTotalComment() {
            return totalComment;
        }

        public void setTotalComment(int totalComment) {
            this.totalComment = totalComment;
        }

        public List<Comment> getComments() {
            return comments;
        }

        public void setComments(List<Comment> comments) {
            this.comments = comments;
        }

        public String getChannelCode() {
            return channelCode;
        }

        public void setChannelCode(String channelCode) {
            this.channelCode = channelCode;
        }

        public long getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(long createdAt) {
            this.createdAt = createdAt;
        }

        public long getLastUpdated() {
            return lastUpdated;
        }

        public void setLastUpdated(long lastUpdated) {
            this.lastUpdated = lastUpdated;
        }
    }

    /**
     * POJO Comment
     */
    public static class Comment extends Post {
        private String postId;
        private String stickerId;
        private String stickerPack;

        public static class Builder implements BuilderPattern<Comment> {
            private String id;
            private String user;
            private String userImage;
            private String content;
            private int totalComment;
            private String postId;
            private String stickerId;
            private String stickerPack;
            private long createdAt;
            private long lastUpdated;

            protected static Builder with(Cursor cursor) {
                Builder builder = new Builder();
                builder.set(cursor);
                return builder;
            }

            private Builder() {
            }

            public Builder(String id) {
                setId(id);
            }

            @Override
            public void set(Cursor cursor) {
                setId(CursorHelper.getStringValue(cursor, KEY_COMMENT_ID));
                setUser(CursorHelper.getStringValue(cursor, KEY_COMMENT_USER));
                setUserImage(CursorHelper.getStringValue(cursor, KEY_COMMENT_USER_IMAGE));
                setContent(CursorHelper.getStringValue(cursor, KEY_COMMENT_CONTENT));
                setTotalComment(CursorHelper.getIntValue(cursor, KEY_COMMENT_TOTAL_COMMENT));
                setPostId(CursorHelper.getStringValue(cursor, KEY_COMMENT_POST_ID));
                setStickerId(CursorHelper.getStringValue(cursor, KEY_COMMENT_STICKER_ID));
                setStickerPack(CursorHelper.getStringValue(cursor, KEY_COMMENT_STICKER_PACK));
                setCreatedAt(CursorHelper.getLongValue(cursor, KEY_COMMENT_CREATED_AT));
                setLastUpdated(CursorHelper.getLongValue(cursor, KEY_COMMENT_LAST_UPDATED));
            }

            private Builder setId(String id) {
                this.id = id;
                return this;
            }

            public Builder setUser(String user) {
                this.user = user;
                return this;
            }

            public Builder setUserImage(String userImage) {
                this.userImage = userImage;
                return this;
            }

            public Builder setContent(String content) {
                this.content = content;
                return this;
            }

            public Builder setTotalComment(int totalComment) {
                this.totalComment = totalComment;
                return this;
            }

            public Builder setPostId(String postId) {
                this.postId = postId;
                return this;
            }

            public Builder setStickerPack(String stickerPack) {
                this.stickerPack = stickerPack;
                return this;
            }

            public Builder setStickerId(String stickerId) {
                this.stickerId = stickerId;
                return this;
            }

            public Builder setCreatedAt(long createdAt) {
                this.createdAt = createdAt;
                return this;
            }

            public Builder setLastUpdated(long lastUpdated) {
                this.lastUpdated = lastUpdated;
                return this;
            }

            @Override
            public Comment build() {
                Comment comment = new Comment(id);
                comment.setUser(user);
                comment.setUserImage(userImage);
                comment.setContent(content);
                comment.setTotalComment(totalComment);
                comment.setPostId(postId);
                comment.setStickerPack(stickerPack);
                comment.setStickerId(stickerId);
                comment.setCreatedAt(createdAt);
                comment.setLastUpdated(lastUpdated);
                return comment;
            }
        }

        public Comment(String id) {
            super(id);
        }

        private Comment() {}

        @Override
        public ContentValues convert() {
            ContentValues values = new ContentValues();

            values.put(KEY_COMMENT_ID, getId());
            values.put(KEY_COMMENT_USER, getUser());
            values.put(KEY_COMMENT_USER_IMAGE, getUserImage());
            values.put(KEY_COMMENT_CONTENT, getContent());
            values.put(KEY_COMMENT_TOTAL_COMMENT, getTotalComment());
            values.put(KEY_COMMENT_POST_ID, getPostId());
            values.put(KEY_COMMENT_STICKER_ID, getStickerId());
            values.put(KEY_COMMENT_STICKER_PACK, getStickerPack());
            values.put(KEY_COMMENT_CREATED_AT, getCreatedAt());
            values.put(KEY_COMMENT_LAST_UPDATED, getLastUpdated());

            return values;
        }

        public String getPostId() {
            return postId;
        }

        public void setPostId(String postId) {
            this.postId = postId;
        }

        public String getStickerId() {
            return stickerId;
        }

        public void setStickerId(String stickerId) {
            this.stickerId = stickerId;
        }

        public String getStickerPack() {
            return stickerPack;
        }

        public void setStickerPack(String stickerPack) {
            this.stickerPack = stickerPack;
        }

        /**
         * Indicates whether some other object is "equal to" this one.
         * <p>
         * The {@code equals} method implements an equivalence relation
         * on non-null object references:
         * <ul>
         * <li>It is <i>reflexive</i>: for any non-null reference value
         * {@code x}, {@code x.equals(x)} should return
         * {@code true}.
         * <li>It is <i>symmetric</i>: for any non-null reference values
         * {@code x} and {@code y}, {@code x.equals(y)}
         * should return {@code true} if and only if
         * {@code y.equals(x)} returns {@code true}.
         * <li>It is <i>transitive</i>: for any non-null reference values
         * {@code x}, {@code y}, and {@code z}, if
         * {@code x.equals(y)} returns {@code true} and
         * {@code y.equals(z)} returns {@code true}, then
         * {@code x.equals(z)} should return {@code true}.
         * <li>It is <i>consistent</i>: for any non-null reference values
         * {@code x} and {@code y}, multiple invocations of
         * {@code x.equals(y)} consistently return {@code true}
         * or consistently return {@code false}, provided no
         * information used in {@code equals} comparisons on the
         * objects is modified.
         * <li>For any non-null reference value {@code x},
         * {@code x.equals(null)} should return {@code false}.
         * </ul>
         * <p>
         * The {@code equals} method for class {@code Object} implements
         * the most discriminating possible equivalence relation on objects;
         * that is, for any non-null reference values {@code x} and
         * {@code y}, this method returns {@code true} if and only
         * if {@code x} and {@code y} refer to the same object
         * ({@code x == y} has the value {@code true}).
         * <p>
         * Note that it is generally necessary to override the {@code hashCode}
         * method whenever this method is overridden, so as to maintain the
         * general contract for the {@code hashCode} method, which states
         * that equal objects must have equal hash codes.
         *
         * @param obj the reference object with which to compare.
         * @return {@code true} if this object is the same as the obj
         * argument; {@code false} otherwise.
         * @see #hashCode()
         * @see HashMap
         */
        @Override
        public boolean equals(Object obj) {
            return obj != null && obj instanceof Comment && this.getId().equals(((Comment) obj).getId());
        }
    }

}
