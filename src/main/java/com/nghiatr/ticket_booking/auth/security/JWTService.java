package com.nghiatr.ticket_booking.auth.security;

import com.nghiatr.ticket_booking.user.model.User;
import com.nghiatr.ticket_booking.user.model.UserRole;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecureDigestAlgorithm;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
@Getter
public class JWTService {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.access-token-expiration-ms}")
    private long accessTokenExpirationMs;

    @Value("${app.jwt.refresh-token-expiration-ms}")
    private long refreshTokenExpirationMs;

    /**
     * Tạo access token chứa các thông tin định danh và quyền hạn của người dùng.
     *
     * @param user thực thể người dùng cần tạo token
     * @return chuỗi JWT access token
     */
    public String generateAccessToken(User user) {
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("email", user.getEmail());
        extraClaims.put("role", user.getRole());
        extraClaims.put("name", user.getName());

        return generateToken(extraClaims, user, accessTokenExpirationMs);
    }

    /**
     * Tạo refresh token cho người dùng với thời gian hết hạn dài hơn.
     *
     * @param user thực thể người dùng cần tạo token
     * @return chuỗi JWT refresh token
     */
    public String generateRefreshToken(User user) {
        return generateToken(new HashMap<>(), user, refreshTokenExpirationMs);
    }

    /**
     * Tạo token JWT với các claims tùy biến, định danh người dùng và thời hạn chỉ định.
     *
     * @param extraClaims bảng chứa các thuộc tính bổ sung cần lưu vào payload
     * @param user thực thể người dùng làm subject
     * @param expirationMs thời gian sống của token tính theo mili-giây
     * @return chuỗi JWT đã được ký số
     */
    public String generateToken(Map<String, Object> extraClaims, User user, long expirationMs) {
        SecureDigestAlgorithm<SecretKey, SecretKey> algorithm = Jwts.SIG.HS256;

        return Jwts.builder()
                .claims(extraClaims)
                .subject(user.getId().toString())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(getSigningKey(), algorithm)
                .compact();
    }

    /**
     * Trích xuất ID của người dùng từ thuộc tính subject trong token.
     *
     * @param token chuỗi JWT
     * @return chuỗi định danh người dùng (userId)
     */
    public String extractUserId(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Trích xuất vai trò của người dùng từ claims trong token.
     *
     * @param token chuỗi JWT
     * @return vai trò người dùng (UserRole)
     */
    public UserRole extractRole(String token) {
        return extractClaim(token, claims -> claims.get("role", UserRole.class));
    }

    /**
     * Trích xuất địa chỉ email của người dùng từ claims trong token.
     *
     * @param token chuỗi JWT
     * @return email của người dùng
     */
    public String extractEmail(String token) {
        return extractClaim(token, claims -> claims.get("email", String.class));
    }

    /**
     * Trích xuất thời điểm hết hạn của token.
     *
     * @param token chuỗi JWT
     * @return thời điểm token hết hạn
     */
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Trích xuất một thuộc tính tùy ý từ claims của token thông qua hàm phân giải.
     *
     * @param token chuỗi JWT
     * @param claimsResolver hàm trích xuất giá trị mong muốn từ Claims
     * @param <T> kiểu dữ liệu của giá trị cần trích xuất
     * @return giá trị trích xuất được
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // Extract the payload.
    private Claims extractAllClaims(String token) {
        return Jwts.parser().
                verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Kiểm tra tính hợp lệ của token đối với người dùng cụ thể và kiểm tra hạn dùng.
     *
     * @param token chuỗi JWT
     * @param user thực thể người dùng cần xác minh
     * @return true nếu token hợp lệ và thuộc về người dùng, ngược lại false
     */
    public boolean isTokenValid(String token, User user) {
        final String userId = extractUserId(token);
        return userId.equals(user.getId().toString()) && !isTokenExpired(token);

    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
