using UnityEngine;


namespace FFmpeg.Demo.REC
{
    public class RecDemoBall : MonoBehaviour
    {
        Rigidbody body;

        void Awake()
        {
            body = GetComponent<Rigidbody>();
        }

        void OnCollisionEnter(Collision collision)
        {
            body.AddForce(Vector3.up * 10, ForceMode.Impulse);
        }
    }
}