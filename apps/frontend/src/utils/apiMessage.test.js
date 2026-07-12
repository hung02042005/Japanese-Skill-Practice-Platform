import { describe, it, expect } from 'vitest';
import { getErrorMessage } from './apiMessage';

describe('getErrorMessage', () => {
  it('ưu tiên message backend trả về trong response.data.message', () => {
    const err = { response: { data: { message: 'Email hoặc mật khẩu không đúng' } } };
    expect(getErrorMessage(err)).toBe('Email hoặc mật khẩu không đúng');
  });

  it('bỏ qua message backend nếu là chuỗi rỗng/toàn khoảng trắng', () => {
    const err = { response: { data: { message: '   ' } }, message: 'Request failed with status code 401' };
    expect(getErrorMessage(err)).toBe('Request failed with status code 401');
  });

  it('trả message riêng cho lỗi mất kết nối mạng (Network Error)', () => {
    const err = { message: 'Network Error' };
    expect(getErrorMessage(err)).toBe('Không kết nối được máy chủ. Kiểm tra mạng và thử lại.');
  });

  it('dùng err.message khi không có response.data.message và không phải Network Error', () => {
    const err = { message: 'Request timeout' };
    expect(getErrorMessage(err)).toBe('Request timeout');
  });

  it('trả về fallback mặc định khi không có thông tin lỗi nào dùng được', () => {
    expect(getErrorMessage({})).toBe('Có lỗi xảy ra, vui lòng thử lại.');
    expect(getErrorMessage(null)).toBe('Có lỗi xảy ra, vui lòng thử lại.');
  });

  it('cho phép override fallback tuỳ theo ngữ cảnh gọi', () => {
    expect(getErrorMessage({}, 'Không tải được danh sách khoá học.')).toBe(
      'Không tải được danh sách khoá học.',
    );
  });
});
