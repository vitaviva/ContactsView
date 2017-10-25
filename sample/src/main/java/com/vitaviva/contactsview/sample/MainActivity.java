package com.vitaviva.contactsview.sample;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.vitaviva.contactsview.ContactsViewActivity;
import com.vitaviva.contactsview.model.LocalContactModel;

import java.util.ArrayList;
import java.util.List;
import java.util.zip.Inflater;

import static com.vitaviva.contactsview.ContactsViewActivity.EXTRA_ARGS;

public class MainActivity extends AppCompatActivity {

    RecyclerView recyclerView;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != ContactsViewActivity.REQUEST_CODE
                || resultCode != Activity.RESULT_OK) return;
        final List<LocalContactModel> list = data.getParcelableArrayListExtra(EXTRA_ARGS);
        RecyclerView.Adapter adapter = new RecyclerView.Adapter() {

            class ViewHolder extends RecyclerView.ViewHolder {
                TextView name;
                TextView phone;

                public ViewHolder(View itemView) {
                    super(itemView);
                    name = (TextView) itemView.findViewById(R.id.name);
                    phone = (TextView) itemView.findViewById(R.id.phone);
                }
            }

            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                return new ViewHolder(getLayoutInflater().inflate(R.layout.recycler_item, parent, false));
            }

            @Override
            public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
                ((ViewHolder) holder).name.setText(list.get(position).getName());
                ((ViewHolder) holder).phone.setText(list.get(position).getTelephone());

            }

            @Override
            public int getItemCount() {
                return list.size();
            }
        };
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.recyclerView = (RecyclerView) findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
                super.onDraw(c, parent, state);
            }

            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                super.getItemOffsets(outRect, view, parent, state);
                outRect.bottom = 5;
            }
        });
        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, ContactsViewActivity.class);
                startActivityForResult(intent, ContactsViewActivity.REQUEST_CODE);
            }
        });
    }
}
