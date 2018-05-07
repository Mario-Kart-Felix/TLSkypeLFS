#import "MediaPlayerBase.h"

@class MediaPlayerNative;

@protocol VideoPlayerDelegate <NSObject>

@optional
- (void)videoPlayerIsReadyToPlayVideo:(MediaPlayerNative *)videoPlayer;
- (void)videoPlayerDidReachEnd:(MediaPlayerNative *)videoPlayer;
- (void)videoPlayer:(MediaPlayerNative *)videoPlayer timeDidChange:(CMTime)cmTime;
- (void)videoPlayer:(MediaPlayerNative *)videoPlayer loadedTimeRangeDidChange:(float)duration;
- (void)videoPlayerPlaybackBufferEmpty:(MediaPlayerNative *)videoPlayer;
- (void)videoPlayerPlaybackLikelyToKeepUp:(MediaPlayerNative *)videoPlayer;
- (void)videoPlayer:(MediaPlayerNative *)videoPlayer didFailWithError:(NSError *)error;

@end

@interface MediaPlayerNative : NSObject<MediaPlayerBase>

@property MediaPlayerNative* instance;
@property AVPlayer *player;
@property AVPlayerItem *playerItem;
@property AVPlayerItemVideoOutput* itemVideoOutput;
@property CVPixelBufferRef textureBuffer;
@property CGColorSpaceRef rgbColorSpace;
@property CIContext *context;

@property BOOL playing;
@property BOOL ready;
@property BOOL seeking;
@property NSURL* url;
@property CGSize videoSize;
@property (nonatomic) int duration;
@property (nonatomic) int framesCounter;
@property long cachedFramesCounter;
@property int cachedPlaybackTime;

@end
