package puc.myapplication;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import puc.myapplication.databinding.ItemBugBinding;

public class BugAdapter extends RecyclerView.Adapter<BugViewHolder> {
    public interface OnBugClickListener {
        void onBugClick(Bug bug);
    }

    public interface OnBugLongClickListener {
        void onBugLongClick(Bug bug);
    }

    private final ArrayList<Bug> bugs;
    private final OnBugClickListener clickListener;
    private final OnBugLongClickListener longClickListener;

    public BugAdapter(ArrayList<Bug> bugs, OnBugClickListener clickListener,
                      OnBugLongClickListener longClickListener) {
        this.bugs = bugs;
        this.clickListener = clickListener;
        this.longClickListener = longClickListener;
    }

    @NonNull
    @Override
    public BugViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemBugBinding binding = ItemBugBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new BugViewHolder(binding, clickListener, longClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull BugViewHolder holder, int position) {
        holder.bind(bugs.get(position));
    }

    @Override
    public int getItemCount() {
        return bugs.size();
    }
}
