package com.boot.util;

import com.boot.domain.User; // User 엔티티의 패키지 경로 확인 (올바름)
// import com.boot.security.CustomUserDetails; // ⭐ CustomUserDetails 임포트 제거
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails; // Spring Security의 UserDetails 임포트

public class SecurityUtil {

    /**
     * 현재 로그인된 사용자의 ID (Long 타입)를 반환합니다.
     * 인증되지 않은 사용자이거나 ID를 찾을 수 없는 경우 null을 반환합니다.
     *
     * @return 현재 로그인된 사용자의 ID (Long) 또는 null
     */
    public static Long getCurrentUserId() {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            // 인증 정보가 없거나, 인증되지 않았거나, 익명 사용자 (로그인 안 된 상태)
            return null;
        }

        Object principal = authentication.getPrincipal();

        // ----------------------------------------------------------------------
        // ★★★ 이 부분을 User 엔티티에서 ID를 가져오도록 수정합니다. ★★★
        // ----------------------------------------------------------------------

        // principal이 User 타입인지 확인하고, 그렇다면 User에서 ID를 가져옵니다.
        // User가 UserDetails를 구현했으므로 이 instanceof 검사가 올바르게 작동합니다.
        if (principal instanceof User) {
            return ((User) principal).getId(); // User 엔티티의 getId() 메서드 호출
        }
        // UserDetails 타입이지만 User 타입은 아닌 경우 (다른 UserDetails 구현체가 있을 경우)
        // 현재는 User가 UserDetails를 직접 구현하므로 이 부분은 거의 실행되지 않을 것입니다.
        else if (principal instanceof UserDetails) {
            // Spring Security의 기본 UserDetails 객체에서 username을 ID로 파싱 시도 (권장하지 않음, username은 보통 로그인 ID)
            // 여기서는 username이 숫자가 아니므로 User가 아닌 다른 UserDetails 구현체일 때만 의미가 있습니다.
            try {
                // 이 부분은 User 엔티티의 username 필드가 숫자 ID를 저장하지 않는 이상,
                // 항상 NumberFormatException이 발생할 것입니다.
                // 따라서 User가 UserDetails를 구현하는 시나리오에서는 이 else-if 블록은 거의 의미가 없습니다.
                // 명시적으로 null을 반환하도록 처리하거나, 필요에 따라 제거하는 것을 권장합니다.
                return Long.parseLong(((UserDetails) principal).getUsername());
            } catch (NumberFormatException e) {
                return null; // username이 숫자가 아닌 경우
            }
        }

        // 위의 어떤 케이스에도 해당하지 않거나, ID를 추출할 수 없는 경우
        return null;
    }

    /**
     * 현재 로그인된 사용자의 UserDetails 객체 (실제로는 User 엔티티)를 반환합니다.
     *
     * @return 현재 로그인된 사용자의 User 객체 (UserDetails 타입) 또는 null
     */
    public static UserDetails getCurrentUserDetails() {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal() == null) {
            return null;
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails) { // User 엔티티도 UserDetails의 인스턴스입니다.
            return (UserDetails) principal;
        }
        return null;
    }

    /**
     * 현재 로그인된 사용자의 User 엔티티 자체를 반환합니다.
     * 이 메서드는 User 엔티티가 UserDetails를 직접 구현하므로 매우 유용합니다.
     *
     * @return 현재 로그인된 사용자의 User 엔티티 또는 null
     */
    public static User getCurrentUser() {
        UserDetails userDetails = getCurrentUserDetails();
        if (userDetails instanceof User) { // UserDetails가 실제 User 타입인지 확인
            return (User) userDetails; // UserDetails를 User로 안전하게 캐스팅
        }
        return null;
    }
}
//package com.boot.util;
//
//import com.boot.domain.User; // User 엔티티의 패키지 경로가 맞는지 다시 한번 확인해주세요.
//import com.boot.security.CustomUserDetails; // ⭐ CustomUserDetails 임포트. 이 경로가 실제 CustomUserDetails의 패키지 경로와 정확히 일치해야 합니다!
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.security.core.userdetails.UserDetails; // Spring Security의 UserDetails 임포트
//
//public class SecurityUtil {
//
//    /**
//     * 현재 로그인된 사용자의 ID (Long 타입)를 반환합니다.
//     * 인증되지 않은 사용자이거나 ID를 찾을 수 없는 경우 null을 반환합니다.
//     *
//     * @return 현재 로그인된 사용자의 ID (Long) 또는 null
//     */
//    public static Long getCurrentUserId() {
//        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//
//        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
//            // 인증 정보가 없거나, 인증되지 않았거나, 익명 사용자 (로그인 안 된 상태)
//            return null;
//        }
//
//        Object principal = authentication.getPrincipal();
//
//        // ----------------------------------------------------------------------
//        // ★★★ 이 부분을 CustomUserDetails에서 User ID를 가져오도록 수정합니다. ★★★
//        // ----------------------------------------------------------------------
//
//        // CustomUserDetails 타입인지 확인하고, 그렇다면 CustomUserDetails에서 ID를 가져옵니다.
//        if (principal instanceof CustomUserDetails) {
//            return ((CustomUserDetails) principal).getId(); // CustomUserDetails에 구현한 getId() 메서드 호출
//        }
//        //
//        // 혹시 모를 다른 상황 (예: OAuth2 로그인 등에서 다른 Principal 타입을 반환하는 경우)을 위해
//        // UserDetails 기본 타입에 대한 처리도 남겨둘 수는 있지만,
//        // 현재 구성에서는 CustomUserDetails가 반환되므로 아래 코드는 거의 호출되지 않습니다.
//        // 필요에 따라 제거하거나 주석 처리해도 무방합니다.
//        else if (principal instanceof UserDetails) {
//            // Spring Security의 기본 UserDetails 객체에서 username을 ID로 파싱 시도 (권장X, username은 보통 로그인 ID)
//            try {
//                return Long.parseLong(((UserDetails) principal).getUsername());
//            } catch (NumberFormatException e) {
//                return null; // username이 숫자가 아닌 경우
//            }
//        }
//
//        // 위의 어떤 케이스에도 해당하지 않거나, ID를 추출할 수 없는 경우
//        return null;
//    }
//
//    /**
//     * (옵션) 현재 로그인된 사용자의 UserDetails 객체를 반환합니다.
//     * 이를 통해 사용자 정의 CustomUserDetails 객체에 접근하여 더 많은 사용자 정보를 얻을 수 있습니다.
//     *
//     * @return 현재 로그인된 사용자의 UserDetails 객체 (CustomUserDetails 타입) 또는 null
//     */
//    public static UserDetails getCurrentUserDetails() {
//        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//
//        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal() == null) {
//            return null;
//        }
//
//        Object principal = authentication.getPrincipal();
//        if (principal instanceof UserDetails) { // CustomUserDetails도 UserDetails의 인스턴스입니다.
//            return (UserDetails) principal;
//        }
//        return null;
//    }
//
//    /**
//     * (옵션) 현재 로그인된 사용자의 User 엔티티 자체를 반환합니다.
//     * 이 메서드는 CustomUserDetails가 실제 User 엔티티를 포함하고 있을 때 유용합니다.
//     *
//     * @return 현재 로그인된 사용자의 User 엔티티 또는 null
//     */
//    public static User getCurrentUser() {
//        UserDetails userDetails = getCurrentUserDetails();
//        if (userDetails instanceof CustomUserDetails) { // CustomUserDetails 타입인지 확인
//            return ((CustomUserDetails) userDetails).getUser(); // CustomUserDetails의 getUser() 메서드 호출
//        }
//        return null;
//    }
//}