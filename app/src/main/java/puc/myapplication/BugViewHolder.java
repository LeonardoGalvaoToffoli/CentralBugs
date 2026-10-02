package puc.myapplication;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Paint;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import puc.myapplication.databinding.ItemBugBinding;

public class BugViewHolder extends RecyclerView.ViewHolder {
    private final ItemBugBinding binding;
    private Bug bugAtual;

    public BugViewHolder(ItemBugBinding binding, BugAdapter.OnBugClickListener clickListener,
                         BugAdapter.OnBugLongClickListener longClickListener) {
        super(binding.getRoot());
        this.binding = binding;
        binding.getRoot().setOnClickListener(view -> clickListener.onBugClick(bugAtual));
        binding.getRoot().setOnLongClickListener(view -> {
            longClickListener.onBugLongClick(bugAtual);
            return true;
        });
    }

    public void bind(Bug bug) {
        bugAtual = bug;
        Context context = binding.getRoot().getContext();
        boolean corrigido = bug.isCorrigido();
        int gravidade = bug.getGravidade();
        int faixa = gravidade <= 2 ? R.string.severity_low
                : gravidade == 3 ? R.string.severity_medium
                : gravidade == 4 ? R.string.severity_high : R.string.severity_critical;
        int corGravidade = ContextCompat.getColor(context, gravidade <= 2 ? R.color.severity_low
                : gravidade == 3 ? R.color.severity_medium : R.color.severity_high);
        int corStatus = ContextCompat.getColor(context,
                corrigido ? R.color.status_resolved : R.color.status_open);
        int corSecundaria = ContextCompat.getColor(context, R.color.text_secondary);

        binding.textBugCode.setText(bug.getCodigo());
        binding.textBugTitle.setText(bug.getTitulo());
        binding.textBugModule.setText(context.getString(R.string.bug_module_value, bug.getModulo()));
        binding.textBugDescription.setText(bug.getDescricao());
        binding.textBugSeverity.setText(context.getString(
                R.string.bug_severity_value, gravidade, context.getString(faixa)));
        binding.textBugStatus.setText(corrigido
                ? R.string.status_resolved_text : R.string.status_open_text);
        binding.imageBug.setImageResource(bug.getImagemResId());
        binding.imageBug.setContentDescription(context.getString(
                R.string.bug_image_description, bug.getCodigo()));
        binding.imageBug.setImageTintList(ColorStateList.valueOf(corrigido ? corStatus : corGravidade));

        // Atribuir os dois estados em cada bind evita resíduos dos cartões reciclados.
        binding.getRoot().setCardBackgroundColor(ContextCompat.getColor(context,
                corrigido ? R.color.resolved_background : R.color.surface_background));
        binding.getRoot().setStrokeColor(corrigido ? corStatus : corGravidade);
        binding.textBugCode.setTextColor(corSecundaria);
        binding.textBugTitle.setTextColor(ContextCompat.getColor(context,
                corrigido ? R.color.text_secondary : R.color.text_primary));
        binding.textBugModule.setTextColor(corSecundaria);
        binding.textBugDescription.setTextColor(corSecundaria);
        binding.textBugSeverity.setTextColor(corGravidade);
        binding.textBugStatus.setTextColor(corStatus);
        int flags = binding.textBugTitle.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG;
        binding.textBugTitle.setPaintFlags(corrigido ? flags | Paint.STRIKE_THRU_TEXT_FLAG : flags);
    }
}
