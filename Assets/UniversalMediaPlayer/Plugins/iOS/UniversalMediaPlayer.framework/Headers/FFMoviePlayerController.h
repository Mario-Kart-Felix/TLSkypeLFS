/*
 * FFMoviePlayerController.h
 *
 * Copyright (c) 2013 Zhang Rui <bbcallen@gmail.com>
 *
 * This file is part of ijkPlayer.
 *
 * ijkPlayer is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * ijkPlayer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with ijkPlayer; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 */

#import "MediaPlayback.h"
#import "FFMonitor.h"
#import "FFOptions.h"

// media meta
#define k_M_KEY_FORMAT         @"format"
#define k_M_KEY_DURATION_US    @"duration_us"
#define k_M_KEY_START_US       @"start_us"
#define k_M_KEY_BITRATE        @"bitrate"

// stream meta
#define k_M_KEY_TYPE           @"type"
#define k_IJKM_VAL_TYPE__VIDEO    @"video"
#define k_IJKM_VAL_TYPE__AUDIO    @"audio"
#define k_IJKM_VAL_TYPE__UNKNOWN  @"unknown"

#define k_M_KEY_CODEC_NAME      @"codec_name"
#define k_M_KEY_CODEC_PROFILE   @"codec_profile"
#define k_M_KEY_CODEC_LONG_NAME @"codec_long_name"

// stream: video
#define k_M_KEY_WIDTH          @"width"
#define k_M_KEY_HEIGHT         @"height"
#define k_M_KEY_FPS_NUM        @"fps_num"
#define k_M_KEY_FPS_DEN        @"fps_den"
#define k_M_KEY_TBR_NUM        @"tbr_num"
#define k_M_KEY_TBR_DEN        @"tbr_den"
#define k_M_KEY_SAR_NUM        @"sar_num"
#define k_M_KEY_SAR_DEN        @"sar_den"
// stream: audio
#define k_M_KEY_SAMPLE_RATE    @"sample_rate"
#define k_M_KEY_CHANNEL_LAYOUT @"channel_layout"

#define kk_M_KEY_STREAMS       @"streams"

typedef enum LogLevel {
    k_LOG_UNKNOWN = 0,
    k_LOG_DEFAULT = 1,

    k_LOG_VERBOSE = 2,
    k_LOG_DEBUG   = 3,
    k_LOG_INFO    = 4,
    k_LOG_WARN    = 5,
    k_LOG_ERROR   = 6,
    k_LOG_FATAL   = 7,
    k_LOG_SILENT  = 8,
} LogLevel;

@interface FFMoviePlayerController : NSObject <MediaPlayback>

- (id)initWithContentURL:(NSURL *)aUrl
             withOptions:(FFOptions *)options;

- (id)initWithContentURLString:(NSString *)aUrlString
                   withOptions:(FFOptions *)options;

- (id)initWithOptions:(FFOptions *)options;

- (void)prepareToPlay;
- (void)play;
- (void)pause;
- (void)stop;
- (BOOL)isPlaying;

- (void)setDataSourceURL:(NSURL *)aUrl;
- (bool)isReady;
- (CVPixelBufferRef)videoBuffer;
- (long)frameCount;
- (int)videoWidth;
- (int)videoHeight;

- (void)setPauseInBackground:(BOOL)pause;
- (BOOL)isVideoToolboxOpen;

+ (void)setLogReport:(BOOL)preferLogReport;
+ (void)setLogLevel:(LogLevel)logLevel;
+ (BOOL)checkIfFFmpegVersionMatch:(BOOL)showAlert;
+ (BOOL)checkIfPlayerVersionMatch:(BOOL)showAlert
                            version:(NSString *)version;

@property(nonatomic, readonly) CGFloat fpsInMeta;
@property(nonatomic, readonly) CGFloat fpsAtOutput;
@property(nonatomic) BOOL shouldShowHudView;

- (void)setOptionValue:(NSString *)value
                forKey:(NSString *)key
            ofCategory:(FFOptionCategory)category;

- (void)setOptionIntValue:(int64_t)value
                   forKey:(NSString *)key
               ofCategory:(FFOptionCategory)category;



- (void)setFormatOptionValue:       (NSString *)value forKey:(NSString *)key;
- (void)setCodecOptionValue:        (NSString *)value forKey:(NSString *)key;
- (void)setSwsOptionValue:          (NSString *)value forKey:(NSString *)key;
- (void)setPlayerOptionValue:       (NSString *)value forKey:(NSString *)key;

- (void)setFormatOptionIntValue:    (int64_t)value forKey:(NSString *)key;
- (void)setCodecOptionIntValue:     (int64_t)value forKey:(NSString *)key;
- (void)setSwsOptionIntValue:       (int64_t)value forKey:(NSString *)key;
- (void)setPlayerOptionIntValue:    (int64_t)value forKey:(NSString *)key;

@property (nonatomic, retain) id<MediaUrlOpenDelegate> segmentOpenDelegate;
@property (nonatomic, retain) id<MediaUrlOpenDelegate> tcpOpenDelegate;
@property (nonatomic, retain) id<MediaUrlOpenDelegate> httpOpenDelegate;
@property (nonatomic, retain) id<MediaUrlOpenDelegate> liveOpenDelegate;

@property (nonatomic, retain) id<MediaNativeInvokeDelegate> nativeInvokeDelegate;

- (void)didShutdown;

#pragma mark KVO properties
@property (nonatomic, readonly) FFMonitor *monitor;

@end

#define FF_IO_TYPE_READ (1)
void FFIOStatDebugCallback(const char *url, int type, int bytes);
void FFIOStatRegister(void (*cb)(const char *url, int type, int bytes));

void FFIOStatCompleteDebugCallback(const char *url,
                                      int64_t read_bytes, int64_t total_size,
                                      int64_t elpased_time, int64_t total_duration);
void FFIOStatCompleteRegister(void (*cb)(const char *url,
                                            int64_t read_bytes, int64_t total_size,
                                            int64_t elpased_time, int64_t total_duration));
