@echo off
color 0A
echo ==========================================
echo KHOI CHAY DU AN E-LEARNING JLPT
echo ==========================================

:: 1. CHẠY BACKEND
echo [1/2] Dang khoi dong Spring Boot Backend...
start "Backend (Spring Boot)" cmd /k "cd apps\backend && mvn spring-boot:run"

:: Tạm dừng 5 giây để Backend khởi động trước (tránh lỗi Frontend gọi API khi BE chưa sẵn sàng)
ping 127.0.0.1 -n 6 > NUL

:: 2. CHẠY FRONTEND
echo [2/2] Dang khoi dong React Frontend...
start "Frontend (React)" cmd /k "cd apps\frontend && npm run dev"

echo ==========================================
echo DA GUI LENH CHAY! 2 CUA SO CMD SE HIEN LEN.
echo Ban co the dong cua so hien tai nay.
echo ==========================================
pause
exit
