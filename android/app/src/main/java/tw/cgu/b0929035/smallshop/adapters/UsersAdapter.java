package tw.cgu.b0929035.smallshop.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import tw.cgu.b0929035.smallshop.R;
import tw.cgu.b0929035.smallshop.listerners.UserListener;
import tw.cgu.b0929035.smallshop.models.User;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UserViewHolder> {
    private final List<User> users;
    private final UserListener userListener;
    public UsersAdapter(List<User> users, UserListener userListener) {
        this.users = users;
        this.userListener = userListener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_container_user, parent, false);
        return new UserViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        holder.setUserData(users.get(position));
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    class UserViewHolder extends RecyclerView.ViewHolder {
        private final View itemView;
        private final TextView textName, textEmail;
        UserViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            textName = itemView.findViewById(R.id.textName);
            textEmail = itemView.findViewById(R.id.textEmail);
        }
        void setUserData(User user) {
            textName.setText(user.name);
            textEmail.setText(user.email);
            itemView.setOnClickListener(view -> userListener.onUserClicked(user));
        }
    }
}
