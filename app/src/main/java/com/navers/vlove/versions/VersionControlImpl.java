package com.navers.vlove.versions;

public interface VersionControlImpl {
    int VERSION_STATUS_OLDER = -1;
    int VERSION_STATUS_NEWER = 1;
    int VERSION_STATUS_ERROR = -99;

    void onVersionReady(int status);
}
