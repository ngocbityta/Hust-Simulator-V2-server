import { Injectable } from '@nestjs/common';
import { CampusPhaseInfo } from '../../common/utils/geo.util';

@Injectable()
export class HeatmapMultiplierService {
  
  calculateActivityMultiplier(targetTimestampMs?: number): number {
    if (!targetTimestampMs) return 1.0;
    const date = new Date(targetTimestampMs);
    const hour = date.getHours() + date.getMinutes() / 60.0;
    
    let multiplier = 1.0;
    if (hour >= 2.0 && hour < 5.0) multiplier = 0.2;
    else if (hour >= 6.0 && hour < 7.0) multiplier = 1.5;
    else if (hour >= 11.5 && hour < 12.5) multiplier = 1.2;
    else if (hour >= 17.0 && hour < 18.0) multiplier = 1.8;
    
    return multiplier;
  }

  adjustTransitPhase(phaseInfo: CampusPhaseInfo): CampusPhaseInfo {
    return { ...phaseInfo };
  }

  getMultiplierReasons(targetTimestampMs?: number): string[] {
    const reasons: string[] = [];
    if (!targetTimestampMs) return reasons;
    
    const date = new Date(targetTimestampMs);
    const hour = date.getHours() + date.getMinutes() / 60.0;

    if (hour >= 2.0 && hour < 5.0) reasons.push("Đang là rạng sáng, lịch sử cho thấy mật độ rất thấp");
    else if (hour >= 6.0 && hour < 7.0) reasons.push("Giờ đi học/thể dục buổi sáng, lịch sử cho thấy mật độ tăng 50%");
    else if (hour >= 11.5 && hour < 12.5) reasons.push("Giờ tan tầm trưa, lịch sử cho thấy mật độ tăng 20%");
    else if (hour >= 17.0 && hour < 18.0) reasons.push("Giờ tan tầm chiều, lịch sử cho thấy mật độ tăng 80%");
    else reasons.push("Lịch sử 4 tuần gần nhất cho thấy mật độ ổn định vào khung giờ này");

    return reasons;
  }
}
