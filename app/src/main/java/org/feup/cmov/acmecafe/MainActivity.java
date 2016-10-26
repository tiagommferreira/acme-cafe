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
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import org.feup.cmov.acmecafe.MenuList.MenuListFragment;
import org.feup.cmov.acmecafe.Models.Product;
import org.feup.cmov.acmecafe.Models.Voucher;
import org.feup.cmov.acmecafe.OrderList.OrderFragment;
import org.feup.cmov.acmecafe.VoucherList.VoucherListFragment;

import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        MenuListFragment.OnMenuListInteractionListener,
        OrderFragment.OnOrderItemInteracionListener,
        OrderFragment.OnOrderVoucherInteractionListener,
        VoucherListFragment.OnVoucherInteractionListener {

    HashMap<Product,Integer> mCurrentOrder = new HashMap<>();
    ArrayList<Voucher> mOrderVouchers = new ArrayList<>();
    ArrayList<Voucher> mUserVouchers = new ArrayList<>();
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

        final NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        this.getSupportFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                Fragment fragment = getCurrentFragment();
                if(fragment instanceof MenuListFragment) {
                    navigationView.setCheckedItem(R.id.nav_menu_list);
                }
                else if(fragment instanceof OrderFragment) {
                    navigationView.setCheckedItem(R.id.nav_current_order);
                }
            }
        });

        swapFragment(MenuListFragment.newInstance());
    }

    public void setToolbarTitle(String title) {
        this.mToolbar.setTitle(title);
    }

    private Fragment getCurrentFragment() {
        return this.getSupportFragmentManager().findFragmentById(R.id.content_frame);
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
        } else if (id == R.id.nav_current_order) {
            fragmentClass = OrderFragment.class;
        } else if (id == R.id.nav_vouchers) {
            fragmentClass = VoucherListFragment.class;
        } else if (id == R.id.nav_past_transactions) {

        }

        try {
            Fragment fragment = null;
            if(fragmentClass == OrderFragment.class) {
                fragment = OrderFragment.newInstance(mCurrentOrder, mOrderVouchers);
            }
            else if(fragmentClass == VoucherListFragment.class) {
                fragment = VoucherListFragment.newInstance(mUserVouchers);
            }
            else {
                fragment = (Fragment) fragmentClass.newInstance();
            }
            swapFragment(fragment);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void swapFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction ft = fragmentManager.beginTransaction();
        ft.replace(R.id.content_frame, fragment);
        ft.addToBackStack(null);
        ft.commit();
    }

    @Override
    public void onMenuListInteraction(Product item) {
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
    public void onItemRemove(Product item, int pos, RecyclerView.Adapter adapter) {
        if(mCurrentOrder.get(item) > 1) {
            mCurrentOrder.put(item, mCurrentOrder.get(item) - 1);
            adapter.notifyItemChanged(pos);
        }
        else {
            mCurrentOrder.remove(item);
            adapter.notifyItemRemoved(pos);
        }
    }

    @Override
    public void onItemHardRemove(Product item, int pos, RecyclerView.Adapter adapter) {
        mCurrentOrder.remove(item);
        adapter.notifyItemRemoved(pos);
    }

    @Override
    public void onVoucherAdded(Voucher voucher, int pos, RecyclerView.Adapter adapter) {
        voucher.setIsUsed(true);
        mOrderVouchers.add(voucher);
        adapter.notifyItemChanged(pos);

        Snackbar.make(getCurrentFocus(), "Voucher " + voucher.getName() + " added to your current order.", Snackbar.LENGTH_SHORT)
                .setAction("Action", null).show();
    }

    @Override
    public void onVoucherRefreshed(ArrayList<Voucher> userVouchers) {
        mOrderVouchers.clear();
        mUserVouchers = userVouchers;
    }

    @Override
    public void onVoucherRemove(Voucher item, int pos, RecyclerView.Adapter adapter) {
        item.setIsUsed(false);
        this.mOrderVouchers.remove(item);
        adapter.notifyItemRemoved(pos);
    }
}
