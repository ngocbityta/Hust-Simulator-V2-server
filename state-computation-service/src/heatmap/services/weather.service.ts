import { Injectable, Logger } from '@nestjs/common';

export interface WeatherData {
  temp: number;
  rain: number;
}

@Injectable()
export class WeatherService {
  private readonly logger = new Logger(WeatherService.name);
  private weatherCache: { data: WeatherData; timestamp: number } | null = null;
  private readonly CACHE_TTL = 30 * 60 * 1000; // 30 minutes

  async fetchWeather(): Promise<WeatherData | null> {
    if (this.weatherCache && (Date.now() - this.weatherCache.timestamp < this.CACHE_TTL)) {
      return this.weatherCache.data;
    }
    
    try {
      // Hanoi coordinates
      const res = await fetch('https://api.open-meteo.com/v1/forecast?latitude=21.004&longitude=105.844&current=temperature_2m,rain');
      if (res.ok) {
        const data = await res.json();
        const temp = data.current?.temperature_2m || 25;
        const rain = data.current?.rain || 0;
        const weatherData = { temp, rain };
        
        this.weatherCache = { data: weatherData, timestamp: Date.now() };
        this.logger.log(`Weather updated: ${temp}°C, rain: ${rain}mm`);
        return weatherData;
      }
    } catch (err) {
      this.logger.warn(`Failed to fetch weather: ${err}`);
    }
    
    return this.weatherCache ? this.weatherCache.data : null;
  }
}
