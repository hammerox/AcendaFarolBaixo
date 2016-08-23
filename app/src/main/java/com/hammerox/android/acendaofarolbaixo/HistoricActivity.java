package com.hammerox.android.acendaofarolbaixo;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.content.CursorLoader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class HistoricActivity extends AppCompatActivity {

    @BindView(R.id.historic_listview) ListView listview;
    @BindView(R.id.historic_emptyview) TextView emptyLayout;

    private SimpleCursorAdapter adapter;
    private String[] columns = new String[]{HistoricContract.COLUMN_DATE, HistoricContract.COLUMN_TIME};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historic);
        ButterKnife.bind(this);

        adapter = new SimpleCursorAdapter(getBaseContext(),
                R.layout.list_layout,
                null,
                columns,
                new int[] { R.id.item_date , R.id.item_time}, 0);
        listview.setAdapter(adapter);
        listview.setEmptyView(emptyLayout);
        refreshValuesFromContentProvider();
    }

    private void refreshValuesFromContentProvider() {
        CursorLoader cursorLoader = new CursorLoader(getBaseContext(), HistoricProvider.CONTENT_URI,
                null, null, null, null);
        Cursor c = cursorLoader.loadInBackground();
        adapter.swapCursor(c);
    }
}
