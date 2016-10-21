package org.feup.cmov.acmecafe;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import org.feup.cmov.acmecafe.MenuList.MenuListFragment;
import org.feup.cmov.acmecafe.OrderList.OrderFragment;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        MenuListFragment.OnMenuListInteractionListener,
        OrderFragment.OnOrderItemInteracionListener {

    HashMap<CafeItem,Integer> mCurrentOrder = new HashMap<>();
    Toolbar mToolbar;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        Class fragmentClass = MenuListFragment.class;

        if (id == R.id.nav_menu_list) {
            fragmentClass = MenuListFragment.class;
            mToolbar.setTitle("Menu");
        } else if (id == R.id.nav_current_order) {
            fragmentClass = OrderFragment.class;
            mToolbar.setTitle("Current Order");
        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        try {
            Fragment fragment = null;
            if(fragmentClass == OrderFragment.class) {
                fragment = OrderFragment.newInstance(mCurrentOrder);
            }
            else {
                fragment = (Fragment) fragmentClass.newInstance();
            }
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction ft = fragmentManager.beginTransaction();
            ft.replace(R.id.content_frame, fragment);
            ft.addToBackStack(null);
            ft.commit();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onMenuListInteraction(CafeItem item) {
        if(mCurrentOrder.containsKey(item)) {
            mCurrentOrder.put(item, mCurrentOrder.get(item) + 1);
        }
        else {
            mCurrentOrder.put(item, 1);
        }

        Snackbar.make(getCurrentFocus(), "Product " + item.getName() + " added to your current order.", Snackbar.LENGTH_SHORT)
                .setAction("Action", null).show();

    }

    @Override
    public void onItemInteraction(CafeItem item) {

    }
}
