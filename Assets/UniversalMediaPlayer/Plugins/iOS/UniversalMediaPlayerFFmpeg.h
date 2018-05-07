#import <OpenGLES/ES2/gl.h>
#import <OpenGLES/ES2/glext.h>
#import <UIKit/UIKit.h>
#import "UnityAppController.h"

#include <vector>
#include "UnityMetalSupport.h"
#include "MediaPlayerBase.h"
#include "MediaPlayerFFmpeg.h"

@interface UniversalMediaPlayerFFmpeg : NSObject<MediaPlayerDelegate>

@property int renderingAPI;
@property id renderingDevice;
@property UniversalMediaPlayerFFmpeg* instance;
@property id<MediaPlayerBase> player;
@property PlayerState* playerState;
@property NSMutableArray* playerStates;
@property intptr_t texturePointer;
@property void* textureCache;
@property void* texture;
@property bool isBuffering;
@property NSString* videoPath;
@property int cachedVolume;
@property float cachedRate;
@property NSInteger tmpBuffering;
@property int tmpTime;
@property unsigned char* frameBuffer;

@end
