package com.californiaclarks.groceryguru;

import java.util.Locale;

import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.californiaclarks.groceryguru.library.DatabaseHandler;
import com.californiaclarks.groceryguru.library.UserFunctions;

public class GroceryGuru extends FragmentActivity {

	// member variables
	GGPagerAdapter paMain;
	ViewPager vpMain;
	UserFunctions userFunctions;

	// Fragments
	Frige2 f;
	ShopList s;
	Recipe r;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		userFunctions = new UserFunctions();
		f = new Frige2();
		s = new ShopList();
		r = new Recipe();

		if (userFunctions.isLoggedIn(getApplicationContext())) {
			userFunctions.setContext(this);
			setContentView(R.layout.groceryguru);
			// Create the adapter that will return a fragment .
			paMain = new GGPagerAdapter(getSupportFragmentManager());
			// Set up the ViewPager with the sections adapter.
			vpMain = (ViewPager) findViewById(R.id.vpMain);
			vpMain.setAdapter(paMain);
		} else {
			Intent login = new Intent(getApplicationContext(), Login.class);
			login.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(login);
			finish();
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater mi = getMenuInflater();
		mi.inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem menuitem) {

		switch (menuitem.getItemId()) {
		case R.id.logout:
			// logout user and close main activity
			userFunctions = new UserFunctions(this);
			userFunctions.logoutUser(getApplicationContext());
			Intent login = new Intent(getApplicationContext(), Login.class);
			login.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(login);
			finish();
			break;
		case R.id.refresh:
			// refresh frige
			refreshFrige();
			break;
		}

		return false;
	}

	public boolean refreshFrige() {

		// refresh data in database from online
		userFunctions = new UserFunctions(this);
		JSONObject json = userFunctions
				.refreshFrige();

		// pull new data from the DB
		boolean notEmpty = false;
		try {
			DatabaseHandler db = new DatabaseHandler(getApplicationContext());

			db.reset(DatabaseHandler.TABLE_FRIGE);
			JSONObject frige = json.getJSONObject("items");
			int j = 0;
			while (j < frige.length()) {
				notEmpty = true;
				String item = frige.names().getString(j);
				db.addItem(item, frige.getJSONArray(item).getString(0), frige
						.getJSONArray(item).getString(1));
				j++;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		f.setItems(userFunctions.getFrige(getApplicationContext()));
		s.setItems(userFunctions.getFrige(getApplicationContext()));

		return notEmpty;
	}
	
	public boolean refreshRecipe() {
		userFunctions = new UserFunctions(this);
		r.setRecipe(userFunctions.requestRecipe());
		return true;
	}

	// PageAdapter
	public class GGPagerAdapter extends FragmentPagerAdapter {

		// constructor
		public GGPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		// return each fragment
		@Override
		public Fragment getItem(int pos) {
			if (pos == 0)
				return new Scanner(userFunctions);
			else if (pos == 1) {
				f.setItems(userFunctions.getFrige(getApplicationContext()));
				return f;
			} else if (pos == 2) {
				s.setItems(userFunctions.getFrige(getApplicationContext()));
				return s;
			} else if (pos == 3) {				
				return r;
			}
			return null;
		}

		// return fragment count
		@Override
		public int getCount() {
			return 4;
		}

		// return fragment titles
		@Override
		public CharSequence getPageTitle(int pos) {
			Locale l = Locale.getDefault();
			switch (pos) {
			case 0:
				return getString(R.string.camera_title).toUpperCase(l);
			case 1:
				return getString(R.string.frige_title).toUpperCase(l);
			case 2:
				return getString(R.string.shoplist_title).toUpperCase(l);
			case 3:
				return getString(R.string.recipe_title).toUpperCase(l);
			}
			return null;
		}
	}

}
