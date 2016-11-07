//package mad.com.applicationproject;
//
//import android.content.Context;
//import android.support.v7.widget.RecyclerView;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.Button;
//import android.widget.TextView;
//
//import java.util.List;
//
///**
// * Created by kiera on 9/10/2016.
// */
//
//public class MyAdapter extends  RecyclerView.Adapter<MyAdapter.ViewHolder> {
//    private Context mContext;
//    private List<GridItem> mGridList;
//
//    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
//        public TextView name;
//        public Button delete;
//        public Button wider;
//        public Button taller;
//
//        public ViewHolder(View view) {
//            super(view);
//            name = (TextView) view.findViewById(R.id.text_view);
//            delete = (Button) view.findViewById(R.id.delete_btn);
//            wider = (Button) view.findViewById(R.id.wider_btn);
//            taller = (Button) view.findViewById(R.id.taller_btn);
//
//            delete.setOnClickListener(this);
//            wider.setOnClickListener(this);
//            taller.setOnClickListener(this);
//        }
//
//        @Override
//        public void onClick(View v) {
//            int position = getAdapterPosition();
//            GridItem item = mGridList.get(position);
//
//            switch (v.getId()) {
//                case R.id.delete_btn:
//                    mGridList.remove(position);
//                    notifyItemRemoved(position);
//                    break;
//
//                case R.id.wider_btn:
//
//                    break;
//
//                case R.id.taller_btn:
//
//                    break;
//            }
//        }
//    }
//
//    public MyAdapter(Context context, List<GridItem> gridList) {
//        mContext = context;
//        mGridList = gridList;
//    }
//
//    @Override
//    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//        View rowView = LayoutInflater.from(parent.getContext())
//                .inflate(R.layout.grid_item, parent, false);
//        return new ViewHolder(rowView);
//    }
//
//    @Override
//    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
//        GridItem item = mGridList.get(position);
//
//        viewHolder.name.setText(item.getName());
//    }
//
//    @Override
//    public int getItemCount() {
//        return mGridList.size();
//    }
//}