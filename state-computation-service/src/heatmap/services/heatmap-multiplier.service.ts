import { Injectable } from '@nestjs/common';
import { CampusPhaseInfo } from '../../common/utils/geo.util';
import { WeatherData } from './weather.service';

@Injectable()
export class HeatmapMultiplierService {
  
  calculateActivityMultiplier(targetTimestampMs?: number, weather?: WeatherData | null): number {
    if (!targetTimestampMs) return 1.0;
    const date = new Date(targetTimestampMs);
    const hour = date.getHours() + date.getMinutes() / 60.0;
    
    let multiplier = 1.0;
    if (hour >= 2.0 && hour < 5.0) multiplier = 0.2;
    else if (hour >= 6.0 && hour < 7.0) multiplier = 1.5;
    else if (hour >= 11.5 && hour < 12.5) multiplier = 1.2;
    else if (hour >= 17.0 && hour < 18.0) multiplier = 1.8;
    
    if (weather) {
      if (weather.rain > 2.0) multiplier *= 0.5; // Heavy rain
      else if (weather.rain > 0) multiplier *= 0.8; // Light rain
      
      if (weather.temp > 37) multiplier *= 0.6; // Extreme heat
      else if (weather.temp > 35) multiplier *= 0.8; // Hot
      else if (weather.temp < 15) multiplier *= 0.8; // Cold
    }
    
    return multiplier;
  }

  adjustTransitPhase(phaseInfo: CampusPhaseInfo, weather?: WeatherData | null): CampusPhaseInfo {
    const adjusted = { ...phaseInfo }; // Clone to avoid side effects
    
    if (weather) {
      if (weather.rain > 0) {
         adjusted.transitRatio *= 0.6; 
         adjusted.wayDensityMultiplier *= 0.5;
         
         // Using a new array to prevent mutating the original reference
         if (!adjusted.nodeHotspots.includes('CANTEEN')) {
           adjusted.nodeHotspots = [...adjusted.nodeHotspots, 'CANTEEN'];
         }
      } else if (weather.temp > 35) {
         adjusted.transitRatio *= 0.8;
         adjusted.wayDensityMultiplier *= 0.7;
      }
    }
    return adjusted;
  }

  getMultiplierReasons(targetTimestampMs?: number, weather?: WeatherData | null): string[] {
    const reasons: string[] = [];
    if (!targetTimestampMs) return reasons;
    
    const date = new Date(targetTimestampMs);
    const hour = date.getHours() + date.getMinutes() / 60.0;

    if (hour >= 2.0 && hour < 5.0) reasons.push("Đang là rạng sáng, lịch sử cho thấy mật độ rất thấp");
    else if (hour >= 6.0 && hour < 7.0) reasons.push("Giờ đi học/thể dục buổi sáng, lịch sử cho thấy mật độ tăng 50%");
    else if (hour >= 11.5 && hour < 12.5) reasons.push("Giờ tan tầm trưa, lịch sử cho thấy mật độ tăng 20%");
    else if (hour >= 17.0 && hour < 18.0) reasons.push("Giờ tan tầm chiều, lịch sử cho thấy mật độ tăng 80%");
    else reasons.push("Lịch sử 4 tuần gần nhất cho thấy mật độ ổn định vào khung giờ này");

    if (weather) {
      if (weather.rain > 2.0) reasons.push("Trời đang mưa to, dự kiến giảm 50% lượng người di chuyển");
      else if (weather.rain > 0) reasons.push("Trời mưa nhẹ, dự kiến giảm 20% lượng người di chuyển");
      else reasons.push("Trời không mưa, thời tiết thuận lợi cho việc di chuyển");
      
      if (weather.temp > 37) reasons.push("Thời tiết nắng nóng gay gắt, người dùng có xu hướng ở trong nhà");
      else if (weather.temp > 35) reasons.push("Thời tiết oi bức, hạn chế di chuyển ngoài trời");
      else if (weather.temp < 15) reasons.push("Trời lạnh, mật độ di chuyển ngoài trời giảm");
    }

    return reasons;
  }
}

