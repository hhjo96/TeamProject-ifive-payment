package com.spartaifive.commercepayment.domain.point.service;

import com.spartaifive.commercepayment.common.exception.ErrorCode;
import com.spartaifive.commercepayment.common.exception.ServiceErrorException;
import com.spartaifive.commercepayment.domain.order.repository.OrderRepository;
import com.spartaifive.commercepayment.domain.payment.entity.Payment;
import com.spartaifive.commercepayment.domain.payment.entity.PaymentStatus;
import com.spartaifive.commercepayment.domain.payment.repository.PaymentRepository;
import com.spartaifive.commercepayment.domain.point.dto.MembershipUpdateInfo;
import com.spartaifive.commercepayment.domain.point.dto.PointUpdateInfo;
import com.spartaifive.commercepayment.domain.point.entity.Point;
import com.spartaifive.commercepayment.domain.point.entity.PointAudit;
import com.spartaifive.commercepayment.domain.point.entity.PointAuditType;
import com.spartaifive.commercepayment.domain.point.entity.PointStatus;
import com.spartaifive.commercepayment.domain.point.repository.PointAuditRepository;
import com.spartaifive.commercepayment.domain.point.repository.PointRepository;
import com.spartaifive.commercepayment.domain.user.entity.MembershipGrade;
import com.spartaifive.commercepayment.domain.user.entity.User;
import com.spartaifive.commercepayment.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PointSupportService {

    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;
    private final PointRepository pointRepository;
    private final OrderRepository orderRepository;
    private final PointAuditRepository pointAuditRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateUserMembership2(
            Long userId,
            List<MembershipGrade> membershipGrades,
            LocalDateTime paymentConfirmDay
    ) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new ServiceErrorException(ErrorCode.ERR_USER_NOT_FOUND)
        );

        List<Payment> payments = paymentRepository.findByUserId(user.getId());

        BigDecimal confirmedPaymentTotal = BigDecimal.ZERO;

        for (Payment p : payments) {
            if (
                    p.getPaymentStatus().equals(PaymentStatus.PAID) &&
                    p.getPaidAt().isBefore(paymentConfirmDay)
            ) {
                confirmedPaymentTotal = confirmedPaymentTotal.add(p.getActualAmount());
            }
        }

        MembershipGrade userMembership = null;

        for (MembershipGrade membershipGrade : membershipGrades) {
            if (confirmedPaymentTotal.compareTo(membershipGrade.getRequiredPurchaseAmount()) <= 0) {
                userMembership = membershipGrade;
                break;
            }
        }

        if (userMembership != null) {
            user.updateMembership(userMembership);
            userRepository.save(user);
        }
    }


    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateUserMembership(
            List<Long> userIds,
            List<MembershipGrade> membershipGrades,
            LocalDateTime paymentConfirmDay
    ) {
        List<MembershipUpdateInfo> updateInfos = pointRepository.getMembershipUpdateInfo(
                userIds, paymentConfirmDay);

        List<User> users = new ArrayList<>();

        for (MembershipUpdateInfo info : updateInfos) {
            User user = info.user();

            MembershipGrade userMembership = null;

            for (MembershipGrade membershipGrade : membershipGrades) {
                if (info.confirmedPaymentTotal().compareTo(membershipGrade.getRequiredPurchaseAmount()) <= 0) {
                    userMembership = membershipGrade;
                    break;
                }
            }

            if (userMembership != null) {
                user.updateMembership(userMembership);
                users.add(user);
            }
        }

        userRepository.saveAll(users);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateUserPoints2(
            Long userId,
            List<MembershipGrade> membershipGrades,
            LocalDateTime paymentConfirmDay
    ) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new ServiceErrorException(ErrorCode.ERR_USER_NOT_FOUND)
        );

        MembershipGrade membership = user.getMembershipGrade();
        if (membership == null) {
            return;
        }

        List<Point> points = pointRepository.findPointByOwnerUser(user);
        List<PointAudit> audits = new ArrayList<>();

        for (Point point : points) {
            if (
                    point.getPointStatus().equals(PointStatus.NOT_READY_TO_BE_SPENT) &&
                    point.getParentPayment().getPaidAt().isBefore(paymentConfirmDay)
            ) {
                BigDecimal pointAmount = getPointAmountPerPurchase(
                        point.getParentPayment().getActualAmount(),
                        membership.getRate()
                );
                point.initPointAmount(pointAmount);
                point.updatePointStatus(PointStatus.CAN_BE_SPENT);

                PointAudit audit = new PointAudit(
                        user,
                        point.getParentOrder(),
                        point.getParentPayment(),
                        point,
                        PointAuditType.POINT_BECAME_READY,
                        pointAmount
                );

                audits.add(audit);
            }
        }

        pointRepository.saveAll(points);
        pointAuditRepository.saveAll(audits);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateUserPoints(
            List<Long> userIds,
            List<MembershipGrade> membershipGrades,
            LocalDateTime paymentConfirmDay
    ) {
        List<PointUpdateInfo> updateInfos = pointRepository.getPointUpdateInfos(
                userIds, paymentConfirmDay);

        List<Point> pointsToSave = new ArrayList<>();
        List<PointAudit> audits = new ArrayList<>();
        
        for (PointUpdateInfo info : updateInfos) {
            if (info.membershipGrade() == null) {
                continue;
            }

            Point point = info.point();
            MembershipGrade membership = info.membershipGrade();
            Payment payment = info.payment();

            BigDecimal pointAmount = getPointAmountPerPurchase(
                    point.getParentPayment().getActualAmount(),
                    membership.getRate()
            );

            point.initPointAmount(pointAmount);
            point.updatePointStatus(PointStatus.CAN_BE_SPENT);

            PointAudit audit = new PointAudit(
                    userRepository.getReferenceById(point.getOwnerUser().getId()),
                    orderRepository.getReferenceById(point.getParentOrder().getId()),
                    payment,
                    point,
                    PointAuditType.POINT_BECAME_READY,
                    pointAmount
            );

            audits.add(audit);
            pointsToSave.add(point);
        }

        pointRepository.saveAll(pointsToSave);
        pointAuditRepository.saveAll(audits);
    }

    @Transactional(readOnly = true)
    public BigDecimal calculateUserPoints(
            Long userId, 
            boolean confirmedOnly
    ) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new ServiceErrorException(ErrorCode.ERR_USER_NOT_FOUND)
        );

        List<Point> points = pointRepository.findPointByOwnerUser_Id(userId);
        BigDecimal total = BigDecimal.ZERO;

        for (Point point : points) {
            if (point.getPointStatus().equals(PointStatus.CAN_BE_SPENT)) {
                total = total.add(point.getPointRemaining());
            }

            if (!confirmedOnly && point.getPointStatus().equals(PointStatus.NOT_READY_TO_BE_SPENT)) {
                BigDecimal paymentAmount = point.getParentPayment().getActualAmount();
                Long rate = user.getMembershipGrade().getRate();
                total = total.add(
                        getPointAmountPerPurchase(paymentAmount, rate));
            }
        }

        return total;
    }

    // ============
    // UTIL들
    // ============

    public static BigDecimal getPointAmountPerPurchase(
            BigDecimal paymentAmount,
            Long rate
    ) {
        BigDecimal oneHundred = BigDecimal.valueOf(100);
        BigDecimal point = paymentAmount.multiply(BigDecimal.valueOf(rate));
        point = point.divide(oneHundred, 2, RoundingMode.HALF_UP);

        return point;
    }

    public static List<PointDecrease> decreasePoints(
            List<Point> points,
            BigDecimal pointToSpend
    ) {
        List<PointDecrease> decreases = new ArrayList<>();

        for (Point point : points) {
            BigDecimal from = point.getPointRemaining();
            BigDecimal to;

            boolean doBreak = false;

            if (point.getPointRemaining().compareTo(pointToSpend) < 0) {
                pointToSpend = pointToSpend.subtract(point.getPointRemaining());
                point.updatePointRemaining(BigDecimal.ZERO);
                
                to = BigDecimal.ZERO;
            } else {
                BigDecimal newPointRemaining = point.getPointRemaining();
                newPointRemaining = newPointRemaining.subtract(pointToSpend);
                point.updatePointRemaining(newPointRemaining);

                to = newPointRemaining;

                doBreak = true;
            }
            
            decreases.add(new PointDecrease(point, from, to));

            if (doBreak) {
                break;
            }
        }

        return decreases;
    }

    public static record PointDecrease (
            Point point,
            BigDecimal from,
            BigDecimal to
    ) {}
}
