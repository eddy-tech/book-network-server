package com.registration.book.book_network.core.models;

import com.registration.book.book_network.core.models.common.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class BookTransactionHistory extends BaseEntity {
     @ManyToOne
     @JoinColumn(name = "book_id")
     private Book book;
     @ManyToOne
     @JoinColumn(name = "user_id")
     private User user;

    private boolean returned;
    private boolean returnApproved;
}
