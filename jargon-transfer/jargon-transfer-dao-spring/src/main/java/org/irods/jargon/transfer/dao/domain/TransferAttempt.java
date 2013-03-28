package org.irods.jargon.transfer.dao.domain;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.CascadeType;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;


/**
 * For a <code>TransferAttempt</code>, this is an individual transfer attempt
 * within the transfer
 * 
 * @author lisa
 */
@Entity
@Table(name = "transfer_attempt")
public class TransferAttempt implements Serializable {
    
    private static final long serialVersionUID = 1L;

    @Id()
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long id;

    @ManyToOne(targetEntity = Transfer.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "transfer_id", nullable = false)
    private Transfer transfer;
    
    @Column(name = "transfer_attempt_start")
    private Date attemptStart;

    @Column(name = "transfer_attempt_end")
    private Date attemptEnd;
    
    @Column(name = "transfer_attempt_status")
    @Enumerated(EnumType.STRING)
    private TransferStatus attemptStatus;

    @Column(name = "error_message")
    private String errorMessage;
    
    @OneToMany(mappedBy = "transfer", targetEntity = TransferItem.class, cascade = { CascadeType.ALL }, fetch = FetchType.LAZY)
    @OrderBy("transferredAt")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<TransferItem> transferItems = new HashSet<TransferItem>();


    public TransferAttempt() {
            super();
    }

    public Long getId() {
            return id;
    }

    public void setId(final Long id) {
            this.id = id;
    }
    
    public Transfer getTransfer() {
            return transfer;
    }

    public void setTransfer(
                    final Transfer transfer) {
            this.transfer = transfer;
    }

    public Date getAttemptStart() {
            return attemptStart;
    }

    public void setAttemptStart(final Date attemptStart) {
            this.attemptStart = attemptStart;
    }
    
    public Date getAttemptEnd() {
            return attemptEnd;
    }

    public void setAttemptEnd(final Date attemptEnd) {
            this.attemptEnd = attemptEnd;
    }
    
    public TransferStatus getAttemptStatus() {
            return attemptStatus;
    }

    public void setAttemptStatus(final TransferStatus attemptStatus) {
            this.attemptStatus = attemptStatus;
    }
    
    public String getErrorMessage() {
            return errorMessage;
    }

    public void setErrorMessage(final String errorMessage) {
            this.errorMessage = errorMessage;
    }
    
    public Set<TransferItem> getTransferItems() {
            return transferItems;
    }

    public void setTransferItems(
                    final Set<TransferItem> transferItems) {
            this.transferItems = transferItems;
    }

    @Override
    public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("TransferAttempt:");
            sb.append("\n   id:");
            sb.append(id);
            sb.append("\n   attemptStart:");
            sb.append(attemptStart);
            sb.append("\n   attemptEnd:");
            sb.append(attemptEnd);
            sb.append("\n   attemptStatus:");
		sb.append(attemptStatus);
            sb.append("\n   errorMessage:");
            sb.append(errorMessage);
            sb.append("\n   transferredAt:");

            return sb.toString();
    }
    
}
