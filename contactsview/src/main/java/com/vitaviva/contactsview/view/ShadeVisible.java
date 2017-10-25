package com.vitaviva.contactsview.view;

public interface ShadeVisible {
    int VISIBLE       = 1;
    int INVISIBLE     = 0;
    int SHADEDURATION = 500;

    void shadeInvisible();

    void shadeVisible();
}
