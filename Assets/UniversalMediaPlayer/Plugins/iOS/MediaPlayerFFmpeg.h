#import "MediaPlayerBase.h"
#import <UniversalMediaPlayer/UniversalMediaPlayer.h>

@interface MediaPlayerFFmpeg : NSObject<MediaPlayerBase>

@property MediaPlayerFFmpeg* instance;
@property (atomic, retain) id<MediaPlayback> player;
@property FFOptions* ffOptions;
@property bool playInBackground;
@property NSString* videoPath;
@property bool isBuffering;
@property NSInteger cachedBuffering;
@property float cachedPlaybackTime;
@property unsigned char* frameBuffer;

@end
