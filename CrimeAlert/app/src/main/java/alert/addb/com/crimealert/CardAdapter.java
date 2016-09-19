package alert.addb.com.crimealert;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import java.util.ArrayList;

public class CardAdapter extends RecyclerView.Adapter<CardAdapter.ViewHolder> {
    private ArrayList<CardContent> data;
    //private ClickListener clickListener;
    private int resource;
   static Context context;
    Resources res;
    String img;




    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case

        public View view;
        public ViewHolder(View v) {
            super(v);

            view = v;

        }


    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public CardAdapter(ArrayList<CardContent>data) {
        this.data = data;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public CardAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                   int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.custom_row, parent, false);
        // set the view's size, margins, paddings and layout parameters
        context = parent.getContext();
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i=new Intent(context,UpdateLeadsActivity.class);
                i.putExtra("crimeid",data.get(position).getCrime_id());
                i.putExtra("type",data.get(position).getCrime_type());
                context.startActivity(i);
            }
        });


        TextView crimetype=(TextView)holder.view.findViewById(R.id.crime_type);
        TextView crimedescription=(TextView)holder.view.findViewById(R.id.crime_description);
        TextView crimelocation=(TextView)holder.view.findViewById(R.id.crime_location);
        TextView crimetime=(TextView)holder.view.findViewById(R.id.postTime);
        // need this to fetch the drawable

        crimedescription.setText(data.get(position).getCrime_description());

        crimetype.setText(data.get(position).getCrime_type());
        crimelocation.setText(data.get(position).getCrime_location());
        crimetime.setText(data.get(position).getCrime_time());
    }

//    public void setClickListener(ClickListener clickListener){
//        this.clickListener=clickListener;
//    }
    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return data.size();
    }

//    public interface ClickListener{
//        public void itemClicked(View view,int position);
//    }
}