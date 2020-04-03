package com.doodlyz.vlove.models;

public interface InteractionListener<T> {
    void onContentClick(T data);
    void onContentLongClick(T data);
    void onContentAdded(T data);
    void onContentRemoved(T data);
}
