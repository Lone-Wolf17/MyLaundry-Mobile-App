package com.gmail.korex006.mylaundry;

import android.app.Activity;
import android.content.Intent;
import android.view.View;

import androidx.appcompat.widget.Toolbar;

import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

public class DrawerUtil {
    public static void getDrawer (final Activity activity, Toolbar toolbar) {
        //if you want to update the items at a later time it is recommended to keep it in a variable
        PrimaryDrawerItem drawerEmptyItem = new PrimaryDrawerItem().withIdentifier(0).withName("");
        drawerEmptyItem.withEnabled(false);

        PrimaryDrawerItem drawerItemAddOrder = new PrimaryDrawerItem().withIdentifier(1)
                .withName("Add Order").withIcon(R.drawable.ic_note_add_black_24dp);
        PrimaryDrawerItem drawerItemPriceList = new PrimaryDrawerItem().withIdentifier(3)
                .withName("Price List").withIcon(R.drawable.ic_tube_end);
        PrimaryDrawerItem drawerItemSavedOrder = new PrimaryDrawerItem().withIdentifier(4).
                withName("Saved Orders").withIcon(R.drawable.ic_tube_middle);

        SecondaryDrawerItem drawerItemAdminSettings = new SecondaryDrawerItem().withIdentifier(2)
                .withName("IT Admin").withIcon(R.drawable.ic_settings_applications_black_24dp);

        //create the drawer and remember the `Drawer` result object
        Drawer result = new DrawerBuilder()
                .withActivity(activity)
                .withToolbar(toolbar)
                .withActionBarDrawerToggle(true)
                .withActionBarDrawerToggleAnimated(true)
                .withCloseOnClick(true)
                .withSelectedItem(-1)
                .addDrawerItems(
                        drawerEmptyItem,
                        drawerItemAddOrder,
                        drawerItemPriceList,
                        drawerItemSavedOrder,
                        new DividerDrawerItem(),
                        drawerItemAdminSettings
                )
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        if (drawerItem.getIdentifier() == 1 && !(activity instanceof SearchActivity)) {
                            // load new Order activity
                            Intent intent = new Intent(activity, SearchActivity.class);
                            view.getContext().startActivity(intent);
                        } else if (drawerItem.getIdentifier() == 2 && !(activity instanceof AdminActivity)) {
                            // load Saved CGPA Activity
                            Intent intent = new Intent(activity, AdminActivity.class);
                            view.getContext().startActivity(intent);
                        } else if (drawerItem.getIdentifier() == 3 && !(activity instanceof PriceListActivity)) {
                            // load PriceList Activity
                            Intent intent = new Intent(activity, PriceListActivity.class);
                            view.getContext().startActivity(intent);
                        } else if (drawerItem.getIdentifier() == 4 && !(activity instanceof SavedOrdersActivity)) {
                            // load PriceList Activity
                            Intent intent = new Intent(activity, SavedOrdersActivity.class);
                            view.getContext().startActivity(intent);
                        } else {
                            return false;
                        }
                        return true;
                    }
                })
                .build();

        result.getActionBarDrawerToggle().setDrawerIndicatorEnabled(true);

    }

    public static void getDrawer (final Activity activity) {
        //if you want to update the items at a later time it is recommended to keep it in a variable
        PrimaryDrawerItem drawerEmptyItem = new PrimaryDrawerItem().withIdentifier(0).withName("");
        drawerEmptyItem.withEnabled(false);

        PrimaryDrawerItem drawerItemAddOrder = new PrimaryDrawerItem().withIdentifier(1)
                .withName("Add Order").withIcon(R.drawable.ic_note_add_black_24dp);

        SecondaryDrawerItem drawerItemAdminSettings = new SecondaryDrawerItem().withIdentifier(2)
                .withName("Admin").withIcon(R.drawable.ic_settings_applications_black_24dp);

        //create the drawer and remember the `Drawer` result object
        Drawer result = new DrawerBuilder()
                .withActivity(activity)
                .withTranslucentStatusBar(false)
                .withActionBarDrawerToggle(true)
                .withCloseOnClick(true)
                .withSelectedItem(-1)
                .addDrawerItems(
                        drawerEmptyItem, drawerEmptyItem,
                        drawerItemAddOrder,
                        new DividerDrawerItem(),
                        drawerItemAdminSettings
                )
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        if (drawerItem.getIdentifier() == 1 && !(activity instanceof SearchActivity)) {
                            // load CGPA activity
                            Intent intent = new Intent(activity, SearchActivity.class);
                            view.getContext().startActivity(intent);
                        } else if (drawerItem.getIdentifier() == 2 && !(activity instanceof AdminActivity)) {
                            // load Saved CGPA Activity
                            Intent intent = new Intent(activity, AdminActivity.class);
                            view.getContext().startActivity(intent);
                        } else {
                            return false;
                        }
                        return true;
                    }
                })
                .build();

        result.getActionBarDrawerToggle().setDrawerIndicatorEnabled(true);
    }


}
