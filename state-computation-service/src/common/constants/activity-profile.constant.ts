export interface ActivityKeyframe {
  hour: number;
  multiplier: number;
}

export const DEFAULT_WEEKDAY_PROFILE: ActivityKeyframe[] = [
  { hour: 0.0,  multiplier: 0.02 },  // Deep night — gần như không có ai
  { hour: 5.5,  multiplier: 0.02 },  // Vẫn rất vắng
  { hour: 6.0,  multiplier: 0.10 },  // Người đầu tiên đến (bảo vệ, canteen sáng)
  { hour: 6.5,  multiplier: 0.40 },  // Dòng người bắt đầu
  { hour: 7.0,  multiplier: 0.80 },  // Phần lớn đã đến
  { hour: 7.5,  multiplier: 0.92 },  // Lớp bắt đầu
  { hour: 9.0,  multiplier: 1.0  },  // Morning peak — tất cả đang trong lớp
  { hour: 11.0, multiplier: 0.95 },  // Vẫn cao, sắp hết tiết
  { hour: 11.5, multiplier: 0.80 },  // Bắt đầu tan lớp buổi sáng
  { hour: 12.0, multiplier: 0.65 },  // Lunch — nhiều người ở canteen
  { hour: 12.5, multiplier: 0.60 },  // Đang ăn trưa
  { hour: 13.0, multiplier: 0.65 },  // Bắt đầu quay lại
  { hour: 13.5, multiplier: 0.85 },  // Lớp chiều bắt đầu
  { hour: 14.5, multiplier: 0.95 },  // Chiều đông đủ
  { hour: 15.5, multiplier: 1.0  },  // Afternoon peak
  { hour: 16.5, multiplier: 0.88 },  // Sắp hết giờ
  { hour: 17.0, multiplier: 0.65 },  // Tan lớp ồ ạt
  { hour: 17.5, multiplier: 0.40 },  // Phần lớn đã rời
  { hour: 18.0, multiplier: 0.30 },  // Chỉ còn tự học/lab
  { hour: 19.0, multiplier: 0.22 },  // Evening study
  { hour: 21.0, multiplier: 0.12 },  // Đóng cửa dần
  { hour: 22.0, multiplier: 0.05 },  // Chỉ còn KTX
  { hour: 24.0, multiplier: 0.02 },  // Deep night
];

export function interpolateKeyframes(hour: number, keyframes: ActivityKeyframe[]): number {
  if (!keyframes || keyframes.length === 0) return 1.0;
  
  // Sort keyframes just in case they are not in order
  const sorted = [...keyframes].sort((a, b) => a.hour - b.hour);

  // 1. Clip out of bounds hours
  if (hour <= sorted[0].hour) return sorted[0].multiplier;
  if (hour >= sorted[sorted.length - 1].hour) return sorted[sorted.length - 1].multiplier;

  // 2. Find interval and linearly interpolate
  for (let i = 0; i < sorted.length - 1; i++) {
    const k1 = sorted[i];
    const k2 = sorted[i + 1];
    if (hour >= k1.hour && hour <= k2.hour) {
      if (k2.hour === k1.hour) return k1.multiplier;
      const t = (hour - k1.hour) / (k2.hour - k1.hour);
      return k1.multiplier + (k2.multiplier - k1.multiplier) * t;
    }
  }
  
  return sorted[0].multiplier;
}
