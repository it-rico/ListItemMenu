package zrc.widget.listitemmenu;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Zaric on 2015/1/29.
 */
public class DemoActivity extends ActionBarActivity {
    List<String> arrays = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MenuListView listView = (MenuListView) findViewById(R.id.list);

        for(int i=0; i<30; i++){
            arrays.add("列表项:"+i);
        }

        listView.setAdapter(new ItemMenuAdapter() {

            @Override
            public int getCount() {
                return arrays.size();
            }

            @Override
            public void onDeleteItem(int pos) {
                arrays.remove(pos);
                notifyDataSetChanged();
            }

            @Override
            public ItemMenuView getItemView(final int position, ItemMenuView convertView,
                                             ViewGroup parent) {
                if(convertView==null){
                    convertView = (ItemMenuView) getLayoutInflater().inflate(R.layout.item_view, null);
                }
                View delete = convertView.findViewById(R.id.delete);
                TextView item = (TextView) convertView.findViewById(R.id.item);
                item.setText(arrays.get(position));
                item.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(DemoActivity.this, "单击了 "+position, Toast.LENGTH_SHORT).show();
                    }
                });

                View info = convertView.findViewById(R.id.info);
                info.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(DemoActivity.this, "单击了信息", Toast.LENGTH_SHORT).show();
                    }
                });

                final ItemMenuView finalConvertView = convertView;
                delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finalConvertView.hideMenu();
                        deleteItem(position, finalConvertView);
                    }
                });
                return convertView;
            }
        });
    }
}
