package com.safespace.app;

/** Shared navigation contract for the Safe Space prototype screens. */
interface ScreenNavigator {
    void openScreen(int screenNumber);

    void goBack();
}
